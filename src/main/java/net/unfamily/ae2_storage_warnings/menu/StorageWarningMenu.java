package net.unfamily.ae2_storage_warnings.menu;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.network.IContainerFactory;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.MenuLocators;

import net.unfamily.ae2_storage_warnings.capacity.CapacityWarning;
import net.unfamily.ae2_storage_warnings.intercept.TerminalOpenGuard;

public class StorageWarningMenu extends AbstractContainerMenu {
    public static final int BUTTON_IGNORE = 0;

    private final List<CapacityWarning> warnings;
    private final MenuType<?> pendingMenuType;
    private final MenuHostLocator pendingLocator;

    public StorageWarningMenu(int containerId, Inventory playerInventory, List<CapacityWarning> warnings,
            MenuType<?> pendingMenuType, MenuHostLocator pendingLocator) {
        super(ModMenus.STORAGE_WARNING.get(), containerId);
        this.warnings = List.copyOf(warnings);
        this.pendingMenuType = pendingMenuType;
        this.pendingLocator = pendingLocator;
    }

    public static StorageWarningMenu fromNetwork(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<CapacityWarning> warnings = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            warnings.add(CapacityWarning.read(buf));
        }
        Identifier menuId = buf.readIdentifier();
        MenuType<?> pendingType = BuiltInRegistries.MENU.getValue(menuId);
        MenuHostLocator locator = MenuLocators.readFromPacket(buf);
        return new StorageWarningMenu(containerId, inventory, warnings, pendingType, locator);
    }

    public static IContainerFactory<StorageWarningMenu> factory() {
        return StorageWarningMenu::fromNetwork;
    }

    public static void open(ServerPlayer player, List<CapacityWarning> warnings, MenuType<?> pendingMenuType,
            MenuHostLocator pendingLocator) {
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new StorageWarningMenu(id, inv, warnings, pendingMenuType, pendingLocator),
                Component.translatable("gui.ae2_storage_warnings.warning.title")), buf -> {
                    writeExtraData(buf, warnings, pendingMenuType, pendingLocator);
                });
    }

    private static void writeExtraData(RegistryFriendlyByteBuf buf, List<CapacityWarning> warnings,
            MenuType<?> pendingMenuType, MenuHostLocator pendingLocator) {
        buf.writeVarInt(warnings.size());
        for (CapacityWarning warning : warnings) {
            warning.write(buf);
        }
        Identifier menuId = BuiltInRegistries.MENU.getKey(pendingMenuType);
        if (menuId == null) {
            throw new IllegalStateException("Pending menu type is not registered: " + pendingMenuType);
        }
        buf.writeIdentifier(menuId);
        MenuLocators.writeToPacket(buf, pendingLocator);
    }

    public List<CapacityWarning> getWarnings() {
        return warnings;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_IGNORE && !player.level().isClientSide()) {
            ignoreAndOpen(player);
            return true;
        }
        return false;
    }

    private void ignoreAndOpen(Player player) {
        TerminalOpenGuard.armSkip(player);
        MenuOpener.open(pendingMenuType, player, pendingLocator);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
