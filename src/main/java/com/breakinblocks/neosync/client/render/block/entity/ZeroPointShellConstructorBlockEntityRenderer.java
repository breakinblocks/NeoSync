package com.breakinblocks.neosync.client.render.block.entity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.block.AbstractShellContainerBlock;
import com.breakinblocks.neosync.common.block.SyncBlocks;

@OnlyIn(Dist.CLIENT)
public class ZeroPointShellConstructorBlockEntityRenderer extends ShellConstructorBlockEntityRenderer {
    private static final ResourceLocation ZERO_POINT_SHELL_CONSTRUCTOR_TEXTURE_ID = ResourceLocation.fromNamespaceAndPath(NeoSync.MOD_ID, "textures/block/zero_point_shell_constructor.png");
    private static final BlockState DEFAULT_STATE = SyncBlocks.ZERO_POINT_SHELL_CONSTRUCTOR.get().defaultBlockState()
            .setValue(AbstractShellContainerBlock.HALF, DoubleBlockHalf.LOWER)
            .setValue(AbstractShellContainerBlock.FACING, Direction.SOUTH);

    public ZeroPointShellConstructorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected BlockState getDefaultState() {
        return DEFAULT_STATE;
    }

    @Override
    protected ResourceLocation getTextureId() {
        return ZERO_POINT_SHELL_CONSTRUCTOR_TEXTURE_ID;
    }
}
