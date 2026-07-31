package net.unfamily.ae2_storage_warnings.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

import net.unfamily.ae2_storage_warnings.Ae2StorageWarnings;

/**
 * Loads GuideME registration via reflection so dedicated servers never classload GuideME client APIs.
 */
public final class GuideMeRegistration {
    private static final String IMPL_CLASS = "net.unfamily.ae2_storage_warnings.guide.StorageWarningsGuide";

    private GuideMeRegistration() {
    }

    public static void register() {
        if (FMLEnvironment.dist != Dist.CLIENT || !ModList.get().isLoaded("guideme")) {
            return;
        }
        try {
            Class.forName(IMPL_CLASS).getMethod("registerClient").invoke(null);
        } catch (ReflectiveOperationException e) {
            Ae2StorageWarnings.LOGGER.error("Failed to register GuideME guide", e);
        }
    }
}
