package net.unfamily.ae2_storage_notifications.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.KeyTypeSelectionButton;
import appeng.menu.me.common.MEStorageMenu;

import net.unfamily.ae2_storage_notifications.client.WarningTypeConfigScreen;
import net.unfamily.ae2_storage_notifications.client.WarningTypeSettingsButton;

@Mixin(MEStorageScreen.class)
public abstract class MEStorageScreenMixin {

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void ae2sn$addWarningSettingsButton(MEStorageMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style, CallbackInfo ci) {
        MEStorageScreen<?> self = (MEStorageScreen<?>) (Object) this;
        WarningTypeSettingsButton button = new WarningTypeSettingsButton(
                btn -> self.switchToScreen(new WarningTypeConfigScreen<>((AEBaseScreen<?>) (Object) self)));

        List<Button> toolbar = ((VerticalButtonBarAccessor) ((AEBaseScreenAccessor) self).ae2sn$getVerticalToolbar())
                .ae2sn$getButtons();

        int insertAt = -1;
        for (int i = 0; i < toolbar.size(); i++) {
            if (toolbar.get(i) instanceof KeyTypeSelectionButton) {
                insertAt = i + 1;
                break;
            }
        }

        if (insertAt >= 0) {
            toolbar.add(insertAt, button);
        } else {
            ((AEBaseScreenAccessor) self).ae2sn$addToLeftToolbar(button);
        }
    }
}
