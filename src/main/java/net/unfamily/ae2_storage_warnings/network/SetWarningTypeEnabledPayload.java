package net.unfamily.ae2_storage_warnings.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import net.unfamily.ae2_storage_warnings.settings.PlayerWarningPreferences;

/** C2S: enable/disable capacity warnings for one AE key type. */
public class SetWarningTypeEnabledPayload {
    private final ResourceLocation keyTypeId;
    private final boolean enabled;

    public SetWarningTypeEnabledPayload(ResourceLocation keyTypeId, boolean enabled) {
        this.keyTypeId = keyTypeId;
        this.enabled = enabled;
    }

    public static void encode(SetWarningTypeEnabledPayload msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.keyTypeId);
        buf.writeBoolean(msg.enabled);
    }

    public static SetWarningTypeEnabledPayload decode(FriendlyByteBuf buf) {
        return new SetWarningTypeEnabledPayload(buf.readResourceLocation(), buf.readBoolean());
    }

    public static void handle(SetWarningTypeEnabledPayload msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer == null) {
                return;
            }
            PlayerWarningPreferences.setWarningEnabled(serverPlayer, msg.keyTypeId, msg.enabled);
            ModNetwork.sendToPlayer(serverPlayer,
                    new SyncWarningSettingsPayload(PlayerWarningPreferences.getDisabledIds(serverPlayer)));
        });
        ctx.setPacketHandled(true);
    }

    public ResourceLocation getKeyTypeId() {
        return keyTypeId;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
