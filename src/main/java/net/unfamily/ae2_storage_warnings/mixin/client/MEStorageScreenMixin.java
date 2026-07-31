package net.unfamily.ae2_storage_warnings.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.TypeFilter;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.me.common.MEStorageMenu;

import net.unfamily.ae2_storage_warnings.client.WarningTypeConfigScreen;
import net.unfamily.ae2_storage_warnings.client.WarningTypeSettingsButton;

@Mixin(MEStorageScreen.class)
public abstract class MEStorageScreenMixin {

    @Shadow
    private SettingToggleButton<TypeFilter> filterTypesToggle;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void ae2sn$addWarningSettingsButton(MEStorageMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style, CallbackInfo ci) {
        MEStorageScreen<?> self = (MEStorageScreen<?>) (Object) this;
        AEBaseScreenAccessor accessor = (AEBaseScreenAccessor) self;
        WarningTypeSettingsButton button = new WarningTypeSettingsButton(
                btn -> accessor.ae2sn$switchToScreen(new WarningTypeConfigScreen<>((AEBaseScreen<?>) (Object) self)));

        List<Button> toolbar = ((VerticalButtonBarAccessor) ((AEBaseScreenAccessor) self).ae2sn$getVerticalToolbar())
                .ae2sn$getButtons();

        if (filterTypesToggle != null) {
            int insertAt = toolbar.indexOf(filterTypesToggle);
            if (insertAt >= 0) {
                toolbar.add(insertAt + 1, button);
                return;
            }
        }
        accessor.ae2sn$addToLeftToolbar(button);
    }
}
