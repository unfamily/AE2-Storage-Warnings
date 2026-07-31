package net.unfamily.ae2_storage_notifications.network;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.unfamily.ae2_storage_notifications.Ae2StorageNotifications;
import net.unfamily.ae2_storage_notifications.settings.PlayerWarningPreferences;

/** S2C: full list of disabled warning key-type ids for the local player. */
public record SyncWarningSettingsPayload(Set<String> disabledTypeIds) implements CustomPacketPayload {

    public static final Type<SyncWarningSettingsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Ae2StorageNotifications.MODID, "sync_warning_settings"));

    public static final StreamCodec<FriendlyByteBuf, SyncWarningSettingsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            payload -> List.copyOf(payload.disabledTypeIds()),
            list -> new SyncWarningSettingsPayload(new HashSet<>(list)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncWarningSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> PlayerWarningPreferences.replaceClientCache(payload.disabledTypeIds()));
    }

    public static SyncWarningSettingsPayload fromPlayer(net.minecraft.world.entity.player.Player player) {
        return new SyncWarningSettingsPayload(PlayerWarningPreferences.getDisabledIds(player));
    }
}
