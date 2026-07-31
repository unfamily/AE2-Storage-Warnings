package net.unfamily.ae2_storage_warnings.capacity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.me.cells.BasicCellInventory;
import appeng.menu.locator.MenuLocator;

import net.unfamily.ae2_storage_warnings.Ae2StorageWarnings;
import net.unfamily.ae2_storage_warnings.Config;

public final class CellCapacityScanner {
    private static final Field BASIC_KEY_TYPE_FIELD = findKeyTypeField();

    private CellCapacityScanner() {
    }

    public static List<CapacityWarning> scan(Player player, MenuLocator locator) {
        Aggregator aggregator = new Aggregator();
        int cellsSeen = 0;

        ITerminalHost host = locator.locate(player, ITerminalHost.class);
        IGrid grid = resolveGrid(host);
        if (grid != null) {
            int driveCount = 0;
            // IMPORTANT: IGrid.getMachines(Class) keys by the owner's concrete class
            // (e.g. DriveBlockEntity), not by interfaces like IChestOrDrive.
            for (IGridNode node : grid.getNodes()) {
                Object owner = node.getOwner();
                if (!(owner instanceof IChestOrDrive drive)) {
                    continue;
                }
                driveCount++;
                for (int slot = 0; slot < drive.getCellCount(); slot++) {
                    StorageCell cell = drive.getOriginalCellInventory(slot);
                    if (cell != null) {
                        cellsSeen++;
                        addCell(aggregator, cell);
                    }
                }
            }
            if (Config.loggingEnabled()) {
                Ae2StorageWarnings.LOGGER.debug(
                        "Capacity scan: gridSize={}, drives={}, cellsSeenSoFar={}", grid.size(), driveCount,
                        cellsSeen);
            }
        } else if (Config.loggingEnabled()) {
            Ae2StorageWarnings.LOGGER.debug("Capacity scan: no grid resolved from terminal host {}",
                    host == null ? "null" : host.getClass().getName());
        }

        // Portable cells / item-hosted menus: also count the cell item itself.
        // MenuItemLocator is package-private on AE2 1.20.1; ItemMenuHost is the public path.
        if (host instanceof ItemMenuHost itemHost) {
            ItemStack stack = itemHost.getItemStack();
            if (!stack.isEmpty()) {
                StorageCell cell = StorageCells.getCellInventory(stack, null);
                if (cell != null) {
                    cellsSeen++;
                    addCell(aggregator, cell);
                }
            }
        }

        List<CapacityWarning> warnings = aggregator.toWarnings(Config.thresholdPercent());
        if (Config.loggingEnabled()) {
            Ae2StorageWarnings.LOGGER.debug(
                    "Capacity scan: cellsSeen={}, buckets={}, warnings={}, threshold={}%", cellsSeen,
                    aggregator.buckets.size(), warnings.size(), Config.thresholdPercent());
            for (Map.Entry<AEKeyType, Bucket> entry : aggregator.buckets.entrySet()) {
                Bucket bucket = entry.getValue();
                long freeBytes = Math.max(0, bucket.totalBytes - bucket.usedBytes);
                int remBytes = bucket.totalBytes > 0
                        ? (int) Math.floor(100.0 * freeBytes / bucket.totalBytes)
                        : 100;
                long freeTypes = Math.max(0, bucket.totalTypes - bucket.usedTypes);
                int remTypes = bucket.totalTypes > 0
                        ? (int) Math.floor(100.0 * freeTypes / bucket.totalTypes)
                        : 100;
                Ae2StorageWarnings.LOGGER.debug(
                        "  bucket {} bytes={}/{} ({}% left) types={}/{} ({}% left) typed={}",
                        entry.getKey().getId(), bucket.usedBytes, bucket.totalBytes, remBytes, bucket.usedTypes,
                        bucket.totalTypes, remTypes, bucket.hasTypeLimit);
            }
            for (CapacityWarning warning : warnings) {
                Ae2StorageWarnings.LOGGER.debug("  warning {} {} remaining={}%", warning.keyTypeId(),
                        warning.kind(), warning.remainingPercent());
            }
        }
        return warnings;
    }

    @Nullable
    private static IGrid resolveGrid(@Nullable ITerminalHost host) {
        if (host instanceof IActionHost actionHost) {
            IGridNode node = actionHost.getActionableNode();
            if (node != null) {
                return node.getGrid();
            }
        }
        return null;
    }

    private static void addCell(Aggregator aggregator, StorageCell cell) {
        if (cell instanceof BasicCellInventory basic) {
            AEKeyType keyType = resolveKeyType(basic);
            if (keyType != null) {
                long usedBytes = basic.getUsedBytes();
                long totalBytes = basic.getTotalBytes();
                long usedTypes = basic.getStoredItemTypes();
                long totalTypes = basic.getTotalItemTypes();

                // Trust AE2 LED state when numeric reads disagree (e.g. edge upgrades).
                CellState status = basic.getStatus();
                if (status == CellState.FULL && totalBytes > 0) {
                    usedBytes = totalBytes;
                }
                if (status == CellState.TYPES_FULL && totalTypes > 0) {
                    usedTypes = totalTypes;
                }

                aggregator.addTyped(keyType, usedBytes, totalBytes, usedTypes, totalTypes);
            }
            return;
        }

        tryAddTypelessBytes(aggregator, cell);
    }

    private static void tryAddTypelessBytes(Aggregator aggregator, StorageCell cell) {
        try {
            long total = invokeLong(cell, "getTotalBytes");
            if (total <= 0) {
                if (Config.loggingEnabled()) {
                    Ae2StorageWarnings.LOGGER.debug("Skip typeless {}: totalBytes={}",
                            cell.getClass().getName(), total);
                }
                return;
            }

            Long free = invokeLongOptional(cell, "getFreeBytes");
            long stored = orZero(invokeLongOptional(cell, "getStoredItemCount"));
            long nbt = orZero(invokeLongOptional(cell, "getNbtItemCount"));

            long used;
            if (free != null) {
                // freeBytes uses getStoredItemCount internally; also consider NBT mirror.
                used = Math.max(Math.max(0, total - free), nbt);
            } else {
                used = Math.max(stored, nbt);
            }

            // MEGAThings can scale stored counts; CellState.FULL is authoritative when set.
            CellState status = cell.getStatus();
            if (status == CellState.FULL || (nbt > 0 && nbt >= total) || (stored > 0 && stored >= total)) {
                used = total;
            } else if (used > total) {
                used = total;
            }

            AEKeyType keyType = resolveTypelessKeyType(cell);
            if (Config.loggingEnabled()) {
                Ae2StorageWarnings.LOGGER.debug("Typeless {} key={} used={} total={} status={}",
                        cell.getClass().getSimpleName(), keyType.getId(), used, total, status);
            }
            aggregator.addBytesOnly(keyType, used, total);
        } catch (ReflectiveOperationException e) {
            if (Config.loggingEnabled()) {
                Ae2StorageWarnings.LOGGER.debug("Failed tipeless read for {}: {}",
                        cell.getClass().getName(), e.toString());
            }
        }
    }

    private static AEKeyType resolveTypelessKeyType(StorageCell cell) {
        try {
            Field field = findField(cell.getClass(), "keyType");
            if (field != null) {
                field.setAccessible(true);
                Object value = field.get(cell);
                if (value instanceof AEKeyType keyType) {
                    return keyType;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return AEKeyType.items();
    }

    @Nullable
    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static long invokeLong(Object target, String methodName) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(methodName);
        return ((Number) method.invoke(target)).longValue();
    }

    private static long orZero(@Nullable Long value) {
        return value == null ? 0L : value;
    }

    @Nullable
    private static Long invokeLongOptional(Object target, String methodName) {
        try {
            return invokeLong(target, methodName);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Nullable
    private static AEKeyType resolveKeyType(BasicCellInventory basic) {
        if (BASIC_KEY_TYPE_FIELD == null) {
            return null;
        }
        try {
            return (AEKeyType) BASIC_KEY_TYPE_FIELD.get(basic);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Nullable
    private static Field findKeyTypeField() {
        try {
            Field field = BasicCellInventory.class.getDeclaredField("keyType");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            Ae2StorageWarnings.LOGGER.error("Failed to access BasicCellInventory.keyType", e);
            return null;
        }
    }

    private static final class Aggregator {
        private final Map<AEKeyType, Bucket> buckets = new HashMap<>();

        void addTyped(AEKeyType keyType, long usedBytes, long totalBytes, long usedTypes, long totalTypes) {
            Bucket bucket = buckets.computeIfAbsent(keyType, ignored -> new Bucket());
            bucket.usedBytes += usedBytes;
            bucket.totalBytes += totalBytes;
            bucket.usedTypes += usedTypes;
            bucket.totalTypes += totalTypes;
            bucket.hasTypeLimit = true;
        }

        void addBytesOnly(AEKeyType keyType, long usedBytes, long totalBytes) {
            Bucket bucket = buckets.computeIfAbsent(keyType, ignored -> new Bucket());
            bucket.usedBytes += usedBytes;
            bucket.totalBytes += totalBytes;
        }

        List<CapacityWarning> toWarnings(int threshold) {
            List<CapacityWarning> warnings = new ArrayList<>();
            for (Map.Entry<AEKeyType, Bucket> entry : buckets.entrySet()) {
                Bucket bucket = entry.getValue();
                if (bucket.totalBytes <= 0 && bucket.totalTypes <= 0) {
                    continue;
                }

                if (bucket.hasTypeLimit && bucket.totalTypes > 0) {
                    long freeTypes = Math.max(0, bucket.totalTypes - bucket.usedTypes);
                    int remainingTypesPct = (int) Math.floor(100.0 * freeTypes / bucket.totalTypes);
                    if (remainingTypesPct <= threshold) {
                        warnings.add(new CapacityWarning(entry.getKey().getId(), CapacityWarning.Kind.TYPES,
                                remainingTypesPct));
                        continue;
                    }
                }

                if (bucket.totalBytes > 0) {
                    long freeBytes = Math.max(0, bucket.totalBytes - bucket.usedBytes);
                    int remainingBytesPct = (int) Math.floor(100.0 * freeBytes / bucket.totalBytes);
                    if (remainingBytesPct <= threshold) {
                        warnings.add(new CapacityWarning(entry.getKey().getId(), CapacityWarning.Kind.BYTES,
                                remainingBytesPct));
                    }
                }
            }
            return warnings;
        }
    }

    private static final class Bucket {
        long usedBytes;
        long totalBytes;
        long usedTypes;
        long totalTypes;
        boolean hasTypeLimit;
    }
}
