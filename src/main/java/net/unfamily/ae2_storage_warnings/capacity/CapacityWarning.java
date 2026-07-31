package net.unfamily.ae2_storage_warnings.capacity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import appeng.api.stacks.AEKeyTypes;

public record CapacityWarning(Identifier keyTypeId, Kind kind, int remainingPercent) {
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
        buf.writeIdentifier(keyTypeId);
        buf.writeEnum(kind);
        buf.writeVarInt(remainingPercent);
    }

    public static CapacityWarning read(FriendlyByteBuf buf) {
        return new CapacityWarning(buf.readIdentifier(), buf.readEnum(Kind.class), buf.readVarInt());
    }
}
