package net.unfamily.ae2_storage_warnings.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import net.unfamily.ae2_storage_warnings.menu.ModMenus;

public final class ClientSetup {
    private ClientSetup() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.STORAGE_WARNING.get(), StorageWarningScreen::new);
    }
}
