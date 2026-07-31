package net.unfamily.ae2_storage_warnings.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.style.BackgroundGenerator;
import appeng.client.gui.widgets.AE2Button;

import net.unfamily.ae2_storage_warnings.capacity.CapacityWarning;
import net.unfamily.ae2_storage_warnings.menu.StorageWarningMenu;

public class StorageWarningScreen extends AbstractContainerScreen<StorageWarningMenu> {
    private static final int PADDING = 12;
    private static final int LINE_HEIGHT = 12;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 140;
    private static final int MIN_WIDTH = 220;
    /** Passed to the super constructor; actual rendered height is computed in init() and stored in panelHeight. */
    private static final int MAX_HEIGHT = 300;

    private final List<FormattedCharSequence> warningLines = new ArrayList<>();
    private int panelWidth = MIN_WIDTH;
    private int panelHeight = MAX_HEIGHT;

    public StorageWarningScreen(StorageWarningMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, MIN_WIDTH, MAX_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        warningLines.clear();

        for (CapacityWarning warning : menu.getWarnings()) {
            Component line = switch (warning.kind()) {
                case TYPES -> Component.translatable("gui.ae2_storage_warnings.warning.types",
                        warning.keyTypeName());
                case BYTES -> Component.translatable("gui.ae2_storage_warnings.warning.bytes",
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

        // imageWidth/imageHeight are final (set by constructor); update leftPos/topPos to match actual panel size
        leftPos = (width - panelWidth) / 2;
        topPos = (height - panelHeight) / 2;

        int buttonX = leftPos + (panelWidth - BUTTON_WIDTH) / 2;
        int buttonY = topPos + panelHeight - PADDING - BUTTON_HEIGHT;
        addRenderableWidget(new AE2Button(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable("gui.ae2_storage_warnings.warning.ignore"),
                btn -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                                StorageWarningMenu.BUTTON_IGNORE);
                    }
                }));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        BackgroundGenerator.draw(panelWidth, panelHeight, guiGraphics, leftPos, topPos);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // MC 26+ GuiGraphicsExtractor.text expects opaque ARGB; 24-bit RGB (e.g. 0x404040) has alpha 0.
        int y = PADDING;
        int titleWidth = font.width(title);
        guiGraphics.text(font, title, (panelWidth - titleWidth) / 2, y, 0xFFFF5555, false);
        y += LINE_HEIGHT + 4;

        for (FormattedCharSequence line : warningLines) {
            guiGraphics.text(font, line, PADDING, y, 0xFF404040, false);
            y += LINE_HEIGHT;
        }
    }
}
