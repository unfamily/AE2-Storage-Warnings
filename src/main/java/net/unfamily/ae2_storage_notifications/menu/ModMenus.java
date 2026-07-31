package net.unfamily.ae2_storage_notifications.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.unfamily.ae2_storage_notifications.Ae2StorageNotifications;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU,
            Ae2StorageNotifications.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<StorageWarningMenu>> STORAGE_WARNING = MENU_TYPES
            .register("storage_warning", () -> IMenuTypeExtension.create(StorageWarningMenu.factory()));

    private ModMenus() {
    }
}
