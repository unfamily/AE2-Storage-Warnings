package net.unfamily.ae2_storage_warnings;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

import net.unfamily.ae2_storage_warnings.client.ClientSetup;
import net.unfamily.ae2_storage_warnings.client.GuideMeRegistration;
import net.unfamily.ae2_storage_warnings.menu.ModMenus;
import net.unfamily.ae2_storage_warnings.network.ModNetwork;

@Mod(Ae2StorageWarnings.MODID)
public class Ae2StorageWarnings {
    public static final String MODID = "ae2_storage_warnings";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Ae2StorageWarnings(IEventBus modEventBus, ModContainer modContainer) {
        ModMenus.MENU_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(ModNetwork::register);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            GuideMeRegistration.register();
            ClientSetup.init(modEventBus);
        }
    }
}
