package net.unfamily.ae2_storage_warnings.network;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.unfamily.ae2_storage_warnings.Ae2StorageWarnings;
import net.unfamily.ae2_storage_warnings.settings.PlayerWarningPreferences;

/** C2S: enable/disable capacity warnings for one AE key type. */
public record SetWarningTypeEnabledPayload(Identifier keyTypeId, boolean enabled)
        implements CustomPacketPayload {

    public static final Type<SetWarningTypeEnabledPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Ae2StorageWarnings.MODID, "set_warning_type"));

    public static final StreamCodec<FriendlyByteBuf, SetWarningTypeEnabledPayload> STREAM_CODEC = StreamCodec
            .composite(
                    Identifier.STREAM_CODEC,
                    SetWarningTypeEnabledPayload::keyTypeId,
                    ByteBufCodecs.BOOL,
                    SetWarningTypeEnabledPayload::enabled,
                    SetWarningTypeEnabledPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetWarningTypeEnabledPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            PlayerWarningPreferences.setWarningEnabled(serverPlayer, payload.keyTypeId(), payload.enabled());
            PacketDistributor.sendToPlayer(serverPlayer,
                    new SyncWarningSettingsPayload(PlayerWarningPreferences.getDisabledIds(serverPlayer)));
        });
    }
}
