package net.unfamily.ae2_storage_notifications.capacity;

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
import appeng.api.storage.cells.StorageCell;
import appeng.me.cells.BasicCellInventory;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuHostLocator;

import net.unfamily.ae2_storage_notifications.Ae2StorageNotifications;
import net.unfamily.ae2_storage_notifications.Config;

public final class CellCapacityScanner {
    private static final Field BASIC_KEY_TYPE_FIELD = findKeyTypeField();

    private CellCapacityScanner() {
    }

    public static List<CapacityWarning> scan(Player player, MenuHostLocator locator) {
        Aggregator aggregator = new Aggregator();

        ITerminalHost host = locator.locate(player, ITerminalHost.class);
        IGrid grid = resolveGrid(host);
        if (grid != null) {
            for (IChestOrDrive drive : grid.getMachines(IChestOrDrive.class)) {
                for (int slot = 0; slot < drive.getCellCount(); slot++) {
                    StorageCell cell = drive.getOriginalCellInventory(slot);
                    if (cell != null) {
                        addCell(aggregator, cell);
                    }
                }
            }
        }

        // Portable cells / item-hosted menus: also count the cell item itself.
        if (locator instanceof ItemMenuHostLocator itemLocator) {
            ItemStack stack = itemLocator.locateItem(player);
            if (!stack.isEmpty()) {
                StorageCell cell = StorageCells.getCellInventory(stack, null);
                if (cell != null) {
                    addCell(aggregator, cell);
                }
            }
        }

        List<CapacityWarning> warnings = aggregator.toWarnings(Config.thresholdPercent());
        if (Config.loggingEnabled()) {
            Ae2StorageNotifications.LOGGER.debug("Capacity scan found {} warning(s)", warnings.size());
            for (CapacityWarning warning : warnings) {
                Ae2StorageNotifications.LOGGER.debug("  {} {} remaining={}%", warning.keyTypeId(), warning.kind(),
                        warning.remainingPercent());
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
                aggregator.addTyped(keyType, basic.getUsedBytes(), basic.getTotalBytes(),
                        basic.getStoredItemTypes(), basic.getTotalItemTypes());
            }
            return;
        }

        // Typeless / custom cells (e.g. AE2Things DISK): bytes only, no type limit.
        tryAddTypelessBytes(aggregator, cell);
    }

    private static void tryAddTypelessBytes(Aggregator aggregator, StorageCell cell) {
        try {
            Method totalMethod = cell.getClass().getMethod("getTotalBytes");
            Method storedMethod = cell.getClass().getMethod("getStoredItemCount");
            long total = ((Number) totalMethod.invoke(cell)).longValue();
            long stored = ((Number) storedMethod.invoke(cell)).longValue();
            if (total > 0) {
                aggregator.addBytesOnly(AEKeyType.items(), stored, total);
            }
        } catch (ReflectiveOperationException ignored) {
            // Unknown cell implementation — skip.
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
            Ae2StorageNotifications.LOGGER.error("Failed to access BasicCellInventory.keyType", e);
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
