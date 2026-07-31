package net.unfamily.ae2_storage_notifications.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.style.BackgroundGenerator;
import appeng.client.gui.widgets.AE2Button;

import net.unfamily.ae2_storage_notifications.capacity.CapacityWarning;
import net.unfamily.ae2_storage_notifications.menu.StorageWarningMenu;

public class StorageWarningScreen extends AbstractContainerScreen<StorageWarningMenu> {
    private static final int PADDING = 12;
    private static final int LINE_HEIGHT = 12;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 140;
    private static final int MIN_WIDTH = 220;

    private final List<FormattedCharSequence> warningLines = new ArrayList<>();
    private int panelWidth;
    private int panelHeight;

    public StorageWarningScreen(StorageWarningMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = MIN_WIDTH;
        this.imageHeight = 120;
    }

    @Override
    protected void init() {
        warningLines.clear();

        for (CapacityWarning warning : menu.getWarnings()) {
            Component line = switch (warning.kind()) {
                case TYPES -> Component.translatable("gui.ae2_storage_notifications.warning.types",
                        warning.keyTypeName());
                case BYTES -> Component.translatable("gui.ae2_storage_notifications.warning.bytes",
                        warning.keyTypeName(), warning.remainingPercent());
            };
            warningLines.addAll(font.split(line, MIN_WIDTH - PADDING * 2));
        }

        int contentHeight = LINE_HEIGHT // title
                + 4
                + warningLines.size() * LINE_HEIGHT
                + 10
                + BUTTON_HEIGHT
                + PADDING;

        panelWidth = MIN_WIDTH;
        panelHeight = Math.max(100, contentHeight + PADDING);
        imageWidth = panelWidth;
        imageHeight = panelHeight;
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        int buttonX = leftPos + (panelWidth - BUTTON_WIDTH) / 2;
        int buttonY = topPos + panelHeight - PADDING - BUTTON_HEIGHT;
        addRenderableWidget(new AE2Button(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable("gui.ae2_storage_notifications.warning.ignore"),
                btn -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                                StorageWarningMenu.BUTTON_IGNORE);
                    }
                }));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        BackgroundGenerator.draw(panelWidth, panelHeight, guiGraphics, leftPos, topPos);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int y = PADDING;
        int titleWidth = font.width(title);
        guiGraphics.drawString(font, title, (panelWidth - titleWidth) / 2, y, 0xFF5555, false);
        y += LINE_HEIGHT + 4;

        for (FormattedCharSequence line : warningLines) {
            guiGraphics.drawString(font, line, PADDING, y, 0x404040, false);
            y += LINE_HEIGHT;
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        // No inventory slots / tooltips needed.
    }
}
