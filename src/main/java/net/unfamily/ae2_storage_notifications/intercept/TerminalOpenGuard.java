package net.unfamily.ae2_storage_notifications.intercept;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.me.items.WirelessCraftingTermMenu;

public final class TerminalOpenGuard {
    private static final Set<UUID> SKIP_ONCE = ConcurrentHashMap.newKeySet();

    private TerminalOpenGuard() {
    }

    public static boolean isStorageTerminal(MenuType<?> type) {
        return type == MEStorageMenu.TYPE
                || type == MEStorageMenu.WIRELESS_TYPE
                || type == MEStorageMenu.PORTABLE_ITEM_CELL_TYPE
                || type == MEStorageMenu.PORTABLE_FLUID_CELL_TYPE
                || type == CraftingTermMenu.TYPE
                || type == WirelessCraftingTermMenu.TYPE
                || type == PatternEncodingTermMenu.TYPE;
    }

    public static void armSkip(Player player) {
        SKIP_ONCE.add(player.getUUID());
    }

    public static boolean consumeSkip(Player player) {
        return SKIP_ONCE.remove(player.getUUID());
    }
}
