package com.breakinblocks.neosync.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SyncClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue SYNC_CAMERA_ANIMATION;

    static {
        BUILDER.comment("Camera settings").push("camera");

        SYNC_CAMERA_ANIMATION = BUILDER.comment(
                        "Fly the camera between your body and the shell when you sync or die.",
                        "Set to false to cut straight to the new shell instead.")
                .define("syncCameraAnimation", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static boolean syncCameraAnimation() {
        return !SPEC.isLoaded() || SYNC_CAMERA_ANIMATION.get();
    }
}
