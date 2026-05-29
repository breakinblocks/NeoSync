package com.breakinblocks.neosync.client.render.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.common.block.AbstractShellContainerBlock;
import com.breakinblocks.neosync.common.block.entity.ShellEntity;
import com.breakinblocks.neosync.common.block.entity.ShellStorageBlockEntity;

public class ShellStorageBlockEntityRenderer extends AbstractShellContainerBlockEntityRenderer<ShellStorageBlockEntity, ShellContainerRenderState> {
    public ShellStorageBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ShellContainerRenderState createRenderState() {
        return new ShellContainerRenderState();
    }

    @Override
    public void extractRenderState(ShellStorageBlockEntity blockEntity, ShellContainerRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.facing = blockEntity.getBlockState().getValue(AbstractShellContainerBlock.FACING);
        renderState.isLowerHalf = blockEntity.getBlockState().getValue(AbstractShellContainerBlock.HALF) == DoubleBlockHalf.LOWER;
        renderState.doorOpenProgress = blockEntity.getDoorOpenProgress(partialTick);

        ShellState shell = blockEntity.getShellState();
        renderState.shellProgress = shell == null ? 0F : shell.getProgress();

        if (renderState.isLowerHalf && shell != null && shell.getProgress() >= ShellState.PROGRESS_DONE) {
            ShellEntity shellEntity = this.getOrCreateClientShellEntity(blockEntity);
            if (shellEntity != null) {
                EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                renderState.shellEntityState = dispatcher.extractEntity(shellEntity, partialTick);
            } else {
                renderState.shellEntityState = null;
            }
        } else {
            renderState.shellEntityState = null;
        }
    }

    @Override
    public void submit(ShellContainerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.shellEntityState == null || !state.isLowerHalf) {
            return;
        }
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float yaw = facingYaw(state.facing);
        poseStack.mulPose(new Quaternionf().rotationY((float)Math.toRadians(yaw)));
        dispatcher.submit(state.shellEntityState, camera, 0.0, 0.0, 0.0, poseStack, submitNodeCollector);
        poseStack.popPose();
    }

    private static float facingYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 180F;
            case EAST -> 270F;
            case WEST -> 90F;
            default -> 0F;
        };
    }
}
