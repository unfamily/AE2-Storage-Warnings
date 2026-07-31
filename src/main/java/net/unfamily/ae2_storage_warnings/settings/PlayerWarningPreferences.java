package net.unfamily.ae2_storage_warnings.settings;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import net.unfamily.ae2_storage_warnings.Ae2StorageWarnings;

/**
 * Per-player toggles for which {@link appeng.api.stacks.AEKeyType} capacity warnings are shown.
 * Checked = warnings enabled (default). Stored values are the disabled type ids.
 */
public final class PlayerWarningPreferences {
    private static final String ROOT_KEY = Ae2StorageWarnings.MODID;
    private static final String DISABLED_KEY = "disabled_warning_types";

    /** Client-side mirror of the local player's disabled types. */
    private static final Set<String> CLIENT_DISABLED = ConcurrentHashMap.newKeySet();

    private PlayerWarningPreferences() {
    }

    public static boolean isWarningEnabled(Player player, Identifier keyTypeId) {
        if (player.level().isClientSide()) {
            return !CLIENT_DISABLED.contains(keyTypeId.toString());
        }
        return !readDisabled(player).contains(keyTypeId.toString());
    }

    public static Set<String> getDisabledIds(Player player) {
        if (player.level().isClientSide()) {
            return Set.copyOf(CLIENT_DISABLED);
        }
        return Set.copyOf(readDisabled(player));
    }

    public static void setWarningEnabled(ServerPlayer player, Identifier keyTypeId, boolean enabled) {
        Set<String> disabled = readDisabled(player);
        String id = keyTypeId.toString();
        if (enabled) {
            disabled.remove(id);
        } else {
            disabled.add(id);
        }
        writeDisabled(player, disabled);
    }

    public static void replaceClientCache(Set<String> disabledIds) {
        CLIENT_DISABLED.clear();
        CLIENT_DISABLED.addAll(disabledIds);
    }

    private static Set<String> readDisabled(Player player) {
        var root = player.getPersistentData().getCompound(ROOT_KEY).orElse(new CompoundTag());
        Set<String> disabled = new HashSet<>();
        ListTag list = root.getList(DISABLED_KEY).orElse(new ListTag());
        for (int i = 0; i < list.size(); i++) {
            disabled.add(list.getString(i).orElse(""));
        }
        return disabled;
    }

    private static void writeDisabled(Player player, Set<String> disabled) {
        ListTag list = new ListTag();
        for (String id : disabled) {
            list.add(StringTag.valueOf(id));
        }
        var root = player.getPersistentData().getCompound(ROOT_KEY).orElse(new CompoundTag());
        root.put(DISABLED_KEY, list);
        player.getPersistentData().put(ROOT_KEY, root);
    }
}
