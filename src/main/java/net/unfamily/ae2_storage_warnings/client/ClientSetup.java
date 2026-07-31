package net.unfamily.ae2_storage_warnings.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import net.unfamily.ae2_storage_warnings.menu.ModMenus;

public final class ClientSetup {
    private ClientSetup() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenus.STORAGE_WARNING.get(), StorageWarningScreen::new));
    }
}
