package net.unfamily.ae2_storage_warnings.guide;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import guideme.Guide;
import guideme.GuideItemSettings;

import net.unfamily.ae2_storage_warnings.Ae2StorageWarnings;

public final class StorageWarningsGuide {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    public static final ResourceLocation GUIDE_ID = ResourceLocation
            .fromNamespaceAndPath(Ae2StorageWarnings.MODID, "guide");

    public static final ResourceLocation WARNINGS_PAGE = ResourceLocation
            .fromNamespaceAndPath(Ae2StorageWarnings.MODID, "storage_warnings.md");

    private StorageWarningsGuide() {
    }

    public static void registerClient() {
        if (!ModList.get().isLoaded("guideme")) {
            return;
        }
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        try {
            Guide.builder(GUIDE_ID)
                    .itemSettings(new GuideItemSettings(
                            Optional.of(Component.translatable("item.ae2_storage_warnings.guide")),
                            List.of(Component.translatable("tooltip.ae2_storage_warnings.guide")),
                            Optional.empty()))
                    .build();
            Ae2StorageWarnings.LOGGER.info("GuideME guide registered");
        } catch (Exception e) {
            REGISTERED.set(false);
            Ae2StorageWarnings.LOGGER.error("Failed to register GuideME guide", e);
        }
    }
}
