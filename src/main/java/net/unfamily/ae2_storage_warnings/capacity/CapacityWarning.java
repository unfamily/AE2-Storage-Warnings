package net.unfamily.ae2_storage_warnings.capacity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.api.stacks.AEKeyTypes;

public record CapacityWarning(ResourceLocation keyTypeId, Kind kind, int remainingPercent) {
    public enum Kind {
        TYPES,
        BYTES
    }

    public Component keyTypeName() {
        try {
            return AEKeyTypes.get(keyTypeId).getDescription();
        } catch (IllegalArgumentException e) {
            return Component.literal(keyTypeId.toString());
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(keyTypeId);
        buf.writeEnum(kind);
        buf.writeVarInt(remainingPercent);
    }

    public static CapacityWarning read(FriendlyByteBuf buf) {
        return new CapacityWarning(buf.readResourceLocation(), buf.readEnum(Kind.class), buf.readVarInt());
    }
}
