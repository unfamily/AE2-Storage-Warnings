package net.unfamily.ae2_storage_warnings.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.unfamily.ae2_storage_warnings.Ae2StorageWarnings;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Ae2StorageWarnings.MODID).versioned("1");
        registrar.playToServer(SetWarningTypeEnabledPayload.TYPE, SetWarningTypeEnabledPayload.STREAM_CODEC,
                SetWarningTypeEnabledPayload::handle);
        registrar.playToClient(SyncWarningSettingsPayload.TYPE, SyncWarningSettingsPayload.STREAM_CODEC,
                SyncWarningSettingsPayload::handle);
    }
}
