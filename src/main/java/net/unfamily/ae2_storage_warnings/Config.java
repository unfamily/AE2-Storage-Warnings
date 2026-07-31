package net.unfamily.ae2_storage_warnings;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue THRESHOLD_PERCENT = BUILDER
            .comment("Remaining capacity percentage at or below which a warning is shown (0-100).")
            .defineInRange("thresholdPercent", 10, 0, 100);

    public static final ForgeConfigSpec.BooleanValue LOGGING_ENABLED;

    static {
        BUILDER.push("logging");
        LOGGING_ENABLED = BUILDER
                .comment("Enable debug logging for capacity scans and terminal intercepts.")
                .define("enabled", false);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    public static int thresholdPercent() {
        return THRESHOLD_PERCENT.get();
    }

    public static boolean loggingEnabled() {
        return LOGGING_ENABLED.get();
    }
}
