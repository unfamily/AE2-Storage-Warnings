package net.unfamily.ae2_storage_notifications;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

import net.unfamily.ae2_storage_notifications.client.ClientSetup;
import net.unfamily.ae2_storage_notifications.client.GuideMeRegistration;
import net.unfamily.ae2_storage_notifications.menu.ModMenus;
import net.unfamily.ae2_storage_notifications.network.ModNetwork;

@Mod(Ae2StorageNotifications.MODID)
public class Ae2StorageNotifications {
    public static final String MODID = "ae2_storage_notifications";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Ae2StorageNotifications(IEventBus modEventBus, ModContainer modContainer) {
        ModMenus.MENU_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(ModNetwork::register);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            GuideMeRegistration.register();
            ClientSetup.init(modEventBus);
        }
    }
}
