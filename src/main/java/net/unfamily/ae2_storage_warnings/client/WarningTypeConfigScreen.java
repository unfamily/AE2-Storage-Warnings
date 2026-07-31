package net.unfamily.ae2_storage_warnings.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.util.Icon;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.AEBaseMenu;

import guideme.GuidesCommon;
import guideme.PageAnchor;

import net.unfamily.ae2_storage_warnings.guide.StorageWarningsGuide;
import net.unfamily.ae2_storage_warnings.network.SetWarningTypeEnabledPayload;
import net.unfamily.ae2_storage_warnings.settings.PlayerWarningPreferences;

/**
 * Terminal sub-screen: toggles capacity warnings per {@link AEKeyType}.
 */
public class WarningTypeConfigScreen<C extends AEBaseMenu, P extends AEBaseScreen<C>> extends AESubScreen<C, P> {
    private final WarningTypeCheckboxes keyTypesWidget = new WarningTypeCheckboxes();

    public WarningTypeConfigScreen(P parent) {
        super(parent, "/screens/key_type_selection.json");

        var back = new TabButton(Icon.BACK,
                Component.translatable("gui.ae2_storage_warnings.warning_types.back"),
                btn -> returnToParent());
        widgets.add("back", back);
        widgets.add("keytypes", keyTypesWidget);
        setTextContent("dialog_title",
                Component.translatable("gui.ae2_storage_warnings.warning_types.title"));
    }

    @Override
    protected void openHelp() {
        var player = getPlayer();
        if (player != null) {
            GuidesCommon.openGuide(player, StorageWarningsGuide.GUIDE_ID,
                    PageAnchor.page(StorageWarningsGuide.WARNINGS_PAGE));
        }
    }

    @Nullable
    @Override
    protected PageAnchor getHelpTopic() {
        return PageAnchor.page(StorageWarningsGuide.WARNINGS_PAGE);
    }

    private void setHeight(int height) {
        this.style.getGeneratedBackground().setHeight(height);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        for (var entry : keyTypesWidget.checkboxes.entrySet()) {
            boolean enabled = PlayerWarningPreferences.isWarningEnabled(getPlayer(), entry.getKey().getId());
            entry.getValue().setSelected(enabled);
            entry.getValue().active = true;
        }
    }

    private class WarningTypeCheckboxes implements ICompositeWidget {
        private static final int PADDING = 6;
        private static final int KEY_TYPE_SPACING = AECheckbox.SIZE + PADDING;

        private Rect2i bounds = new Rect2i(0, 0, 0, 0);
        private final Map<AEKeyType, AECheckbox> checkboxes = new LinkedHashMap<>();

        @Override
        public void setPosition(Point position) {
            bounds = new Rect2i(position.getX(), position.getY(), bounds.getWidth(), bounds.getHeight());
        }

        @Override
        public void setSize(int width, int height) {
            bounds = new Rect2i(bounds.getX(), bounds.getY(), width, height);
        }

        @Override
        public Rect2i getBounds() {
            return bounds;
        }

        @Override
        public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
            int xPos = this.bounds.getX() + bounds.getX();
            int yPos = this.bounds.getY() + bounds.getY();
            checkboxes.clear();

            for (AEKeyType keyType : AEKeyTypes.getAll()) {
                var text = keyType.getDescription();
                int textboxWidth = 24 + Minecraft.getInstance().font.width(text);
                var checkbox = new AECheckbox(xPos, yPos, textboxWidth, AECheckbox.SIZE, screen.getStyle(), text);
                checkbox.setChangeListener(() -> ClientPacketDistributor.sendToServer(
                        new SetWarningTypeEnabledPayload(keyType.getId(), checkbox.isSelected())));
                addWidget.accept(checkbox);
                checkboxes.put(keyType, checkbox);
                yPos += KEY_TYPE_SPACING;
            }

            int height = this.bounds.getY() + AEKeyTypes.getAll().size() * KEY_TYPE_SPACING + PADDING;
            WarningTypeConfigScreen.this.setHeight(height);
        }
    }
}
