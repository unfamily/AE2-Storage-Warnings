package net.unfamily.ae2_storage_warnings.network;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import net.unfamily.ae2_storage_warnings.Ae2StorageWarnings;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Ae2StorageWarnings.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private ModNetwork() {
    }

    public static void register() {
        int index = 0;

        CHANNEL.registerMessage(index++,
                SetWarningTypeEnabledPayload.class,
                SetWarningTypeEnabledPayload::encode,
                SetWarningTypeEnabledPayload::decode,
                SetWarningTypeEnabledPayload::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(index++,
                SyncWarningSettingsPayload.class,
                SyncWarningSettingsPayload::encode,
                SyncWarningSettingsPayload::decode,
                SyncWarningSettingsPayload::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }
}
