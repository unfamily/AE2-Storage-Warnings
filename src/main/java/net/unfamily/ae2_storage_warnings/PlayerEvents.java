package net.unfamily.ae2_storage_warnings;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.unfamily.ae2_storage_warnings.network.ModNetwork;
import net.unfamily.ae2_storage_warnings.network.SyncWarningSettingsPayload;
import net.unfamily.ae2_storage_warnings.settings.PlayerWarningPreferences;

@Mod.EventBusSubscriber(modid = Ae2StorageWarnings.MODID)
public final class PlayerEvents {
    private PlayerEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModNetwork.sendToPlayer(serverPlayer,
                    new SyncWarningSettingsPayload(PlayerWarningPreferences.getDisabledIds(serverPlayer)));
        }
    }
}
