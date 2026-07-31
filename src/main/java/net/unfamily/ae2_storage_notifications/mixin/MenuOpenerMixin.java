package net.unfamily.ae2_storage_notifications.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;

import net.unfamily.ae2_storage_notifications.Ae2StorageNotifications;
import net.unfamily.ae2_storage_notifications.Config;
import net.unfamily.ae2_storage_notifications.capacity.CapacityWarning;
import net.unfamily.ae2_storage_notifications.capacity.CellCapacityScanner;
import net.unfamily.ae2_storage_notifications.intercept.TerminalOpenGuard;
import net.unfamily.ae2_storage_notifications.menu.StorageWarningMenu;

@Mixin(MenuOpener.class)
public class MenuOpenerMixin {
    @Inject(method = "open(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/world/entity/player/Player;Lappeng/menu/locator/MenuHostLocator;Z)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ae2StorageNotifications$interceptOpen(MenuType<?> type, Player player, MenuHostLocator locator,
            boolean fromSubMenu, CallbackInfoReturnable<Boolean> cir) {
        if (fromSubMenu || player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!TerminalOpenGuard.isStorageTerminal(type)) {
            return;
        }
        if (TerminalOpenGuard.consumeSkip(player)) {
            if (Config.loggingEnabled()) {
                Ae2StorageNotifications.LOGGER.debug("Skipping warning for {}", player.getGameProfile().getName());
            }
            return;
        }

        List<CapacityWarning> warnings = CellCapacityScanner.scan(player, locator);
        if (warnings.isEmpty()) {
            return;
        }

        if (Config.loggingEnabled()) {
            Ae2StorageNotifications.LOGGER.debug("Intercepting terminal open for {} with {} warning(s)",
                    player.getGameProfile().getName(), warnings.size());
        }

        StorageWarningMenu.open(serverPlayer, warnings, type, locator);
        cir.setReturnValue(true);
    }
}
