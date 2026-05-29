package com.breakinblocks.neosync.client.render.block.entity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class ShellContainerRenderState extends BlockEntityRenderState {
    @Nullable
    public EntityRenderState shellEntityState;
    public Direction facing = Direction.NORTH;
    public boolean isLowerHalf;
    public float doorOpenProgress;
    public float shellProgress = 1F;
}
