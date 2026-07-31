package net.unfamily.ae2_storage_warnings;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue THRESHOLD_PERCENT = BUILDER
            .comment("Remaining capacity percentage at or below which a warning is shown (0-100).")
            .defineInRange("thresholdPercent", 10, 0, 100);

    public static final ModConfigSpec.BooleanValue LOGGING_ENABLED;

    static {
        BUILDER.push("logging");
        LOGGING_ENABLED = BUILDER
                .comment("Enable debug logging for capacity scans and terminal intercepts.")
                .define("enabled", false);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    public static int thresholdPercent() {
        return THRESHOLD_PERCENT.get();
    }

    public static boolean loggingEnabled() {
        return LOGGING_ENABLED.get();
    }
}
