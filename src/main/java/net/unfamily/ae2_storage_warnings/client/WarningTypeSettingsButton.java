package net.unfamily.ae2_storage_warnings.client;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.IconButton;
import appeng.util.Icon;

import net.unfamily.ae2_storage_warnings.Ae2StorageWarnings;

public class WarningTypeSettingsButton extends IconButton {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Ae2StorageWarnings.MODID, "textures/guis/state_warn.png");

    public WarningTypeSettingsButton(OnPress onPress) {
        super(onPress);
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(
                Component.translatable("gui.ae2_storage_warnings.warning_types.button"),
                Component.translatable("gui.ae2_storage_warnings.warning_types.button.detail"));
    }

    @Override
    protected Icon getIcon() {
        return null;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
        if (!visible) {
            return;
        }

        var yOffset = isHovered() ? 1 : 0;
        var blitter = Blitter.texture(TEXTURE, 16, 16).src(0, 0, 16, 16);
        if (!active) {
            blitter.opacity(0.5f);
        }
        blitter.dest(getX(), getY() + 1 + yOffset).blit(guiGraphics);
    }
}
