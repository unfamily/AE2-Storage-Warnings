package net.unfamily.ae2_storage_warnings;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.unfamily.ae2_storage_warnings.client.ClientSetup;
import net.unfamily.ae2_storage_warnings.client.GuideMeRegistration;
import net.unfamily.ae2_storage_warnings.menu.ModMenus;
import net.unfamily.ae2_storage_warnings.network.ModNetwork;

@Mod(Ae2StorageWarnings.MODID)
public class Ae2StorageWarnings {
    public static final String MODID = "ae2_storage_warnings";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Ae2StorageWarnings() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModMenus.MENU_TYPES.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModNetwork.register();

        MinecraftForge.EVENT_BUS.register(PlayerEvents.class);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            GuideMeRegistration.register();
            ClientSetup.init(modEventBus);
        });
    }
}
