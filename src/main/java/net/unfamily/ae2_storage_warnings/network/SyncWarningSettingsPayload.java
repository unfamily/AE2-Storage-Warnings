package net.unfamily.ae2_storage_warnings.network;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import net.unfamily.ae2_storage_warnings.settings.PlayerWarningPreferences;

/** S2C: full list of disabled warning key-type ids for the local player. */
public class SyncWarningSettingsPayload {
    private final Set<String> disabledTypeIds;

    public SyncWarningSettingsPayload(Set<String> disabledTypeIds) {
        this.disabledTypeIds = Set.copyOf(disabledTypeIds);
    }

    public static void encode(SyncWarningSettingsPayload msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.disabledTypeIds.size());
        for (String id : msg.disabledTypeIds) {
            buf.writeUtf(id);
        }
    }

    public static SyncWarningSettingsPayload decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        Set<String> ids = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(buf.readUtf());
        }
        return new SyncWarningSettingsPayload(ids);
    }

    public static void handle(SyncWarningSettingsPayload msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> PlayerWarningPreferences.replaceClientCache(msg.disabledTypeIds));
        ctx.setPacketHandled(true);
    }

    public Set<String> getDisabledTypeIds() {
        return disabledTypeIds;
    }
}
