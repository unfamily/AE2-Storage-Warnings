package net.unfamily.ae2_storage_warnings.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.IconButton;

import net.unfamily.ae2_storage_warnings.Ae2StorageWarnings;

/**
 * Toolbar button with a custom texture. AE2 15.x {@link IconButton} does not null-check
 * {@link #getIcon()}, so we draw the background + sprite ourselves instead of calling super.
 */
public class WarningTypeSettingsButton extends IconButton {
    public static final ResourceLocation TEXTURE = new ResourceLocation(
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
        // Never used: renderWidget does not call super (AE2 15 NPE on null).
        return Icon.TOOLBAR_BUTTON_BACKGROUND;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        if (isFocused()) {
            guiGraphics.fill(getX() - 1, getY() - 1, getX() + width + 1, getY(), 0xFFFFFFFF);
            guiGraphics.fill(getX() - 1, getY(), getX(), getY() + height, 0xFFFFFFFF);
            guiGraphics.fill(getX() + width, getY(), getX() + width + 1, getY() + height, 0xFFFFFFFF);
            guiGraphics.fill(getX() - 1, getY() + height, getX() + width + 1, getY() + height + 1, 0xFFFFFFFF);
        }

        if (!isDisableBackground()) {
            Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(getX(), getY()).blit(guiGraphics);
        }

        var blitter = Blitter.texture(TEXTURE, 16, 16).src(0, 0, 16, 16);
        if (!active) {
            blitter.opacity(0.5f);
        }
        blitter.dest(getX(), getY()).blit(guiGraphics);

        RenderSystem.enableDepthTest();
    }
}
