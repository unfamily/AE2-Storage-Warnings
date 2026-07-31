package net.unfamily.ae2_storage_notifications.client;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.IconButton;

import net.unfamily.ae2_storage_notifications.Ae2StorageNotifications;

public class WarningTypeSettingsButton extends IconButton {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Ae2StorageNotifications.MODID, "textures/guis/state_warn.png");

    public WarningTypeSettingsButton(OnPress onPress) {
        super(onPress);
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(
                Component.translatable("gui.ae2_storage_notifications.warning_types.button"),
                Component.translatable("gui.ae2_storage_notifications.warning_types.button.detail"));
    }

    @Override
    protected Icon getIcon() {
        // Custom texture is drawn in renderWidget; AE2 background still uses super.
        return null;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        if (!visible) {
            return;
        }

        int hoverOffset = isHoveredOrFocused() ? 1 : 0;
        var blitter = Blitter.texture(TEXTURE, 16, 16).src(0, 0, 16, 16);
        if (!active) {
            blitter.opacity(0.5f);
        }
        blitter.dest(getX(), getY() + 1 + hoverOffset).zOffset(3).blit(guiGraphics);
    }
}
