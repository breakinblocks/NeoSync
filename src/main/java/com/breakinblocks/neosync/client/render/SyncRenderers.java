package com.breakinblocks.neosync.client.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import com.breakinblocks.neosync.client.render.block.entity.ShellConstructorBlockEntityRenderer;
import com.breakinblocks.neosync.client.render.block.entity.ShellStorageBlockEntityRenderer;
import com.breakinblocks.neosync.client.render.block.entity.TreadmillBlockEntityRenderer;
import com.breakinblocks.neosync.common.block.entity.SyncBlockEntities;

public final class SyncRenderers {
    private SyncRenderers() {}

    public static void initClient() {
        BlockEntityRenderers.register(SyncBlockEntities.SHELL_STORAGE.get(), ShellStorageBlockEntityRenderer::new);
        BlockEntityRenderers.register(SyncBlockEntities.SHELL_CONSTRUCTOR.get(), ShellConstructorBlockEntityRenderer::new);
        BlockEntityRenderers.register(SyncBlockEntities.TREADMILL.get(), TreadmillBlockEntityRenderer::new);
    }
}
