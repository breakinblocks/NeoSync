package com.breakinblocks.neosync.compat.thirst;

import com.breakinblocks.neosync.api.shell.ShellStateComponentFactoryRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

public final class ThirstCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean LOADED = ModList.get().isLoaded("thirstwastaken2");

    private ThirstCompat() {}

    public static void init() {
        if (!LOADED) {
            return;
        }

        try {
            ShellStateComponentFactoryRegistry.getInstance().register(new ThirstShellStateComponentFactory());
        } catch (Throwable t) {
            LOGGER.warn("Thirst Was Taken 2 is loaded but the shell state integration failed to initialize", t);
        }
    }
}
