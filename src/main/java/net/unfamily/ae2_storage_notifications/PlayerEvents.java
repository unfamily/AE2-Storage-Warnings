package net.unfamily.ae2_storage_notifications;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import net.unfamily.ae2_storage_notifications.network.SyncWarningSettingsPayload;

@EventBusSubscriber(modid = Ae2StorageNotifications.MODID)
public final class PlayerEvents {
    private PlayerEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, SyncWarningSettingsPayload.fromPlayer(serverPlayer));
        }
    }
}
