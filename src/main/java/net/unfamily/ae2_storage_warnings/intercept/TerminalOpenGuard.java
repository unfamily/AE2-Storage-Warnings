package net.unfamily.ae2_storage_warnings.intercept;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.api.storage.ITerminalHost;
import appeng.menu.locator.MenuHostLocator;

/**
 * Detects ME terminal opens, including addon terminals (e.g. AE2WTLib wireless universal),
 * by resolving {@link ITerminalHost} from the AE2 menu locator instead of hardcoding MenuTypes.
 */
public final class TerminalOpenGuard {
    private static final Set<UUID> SKIP_ONCE = ConcurrentHashMap.newKeySet();

    private TerminalOpenGuard() {
    }

    public static boolean shouldIntercept(MenuType<?> type, Player player, MenuHostLocator locator) {
        // Any terminal-like UI is hosted by ITerminalHost (vanilla AE2 + most addons).
        if (locator.locate(player, ITerminalHost.class) == null) {
            return false;
        }

        Identifier id = BuiltInRegistries.MENU.getKey(type);
        if (id == null) {
            return true;
        }

        // Skip utility submenus opened from a terminal with the same locator/host.
        return !isUtilitySubmenu(id.getPath());
    }

    private static boolean isUtilitySubmenu(String path) {
        return path.contains("magnet")
                || path.equals("trash")
                || path.startsWith("trash_")
                || path.contains("priority")
                || path.contains("craft_amount")
                || path.contains("craft_confirm")
                || path.contains("crafting_status")
                || path.contains("crafting_cpu")
                || path.contains("set_stock")
                || path.contains("set_processing")
                || path.contains("network_tool")
                || path.contains("network_status")
                || path.contains("cell_workbench");
    }

    public static void armSkip(Player player) {
        SKIP_ONCE.add(player.getUUID());
    }

    public static boolean consumeSkip(Player player) {
        return SKIP_ONCE.remove(player.getUUID());
    }
}
