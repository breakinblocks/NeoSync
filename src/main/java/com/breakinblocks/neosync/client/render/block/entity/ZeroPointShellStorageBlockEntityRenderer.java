package com.breakinblocks.neosync.client.render.block.entity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.block.AbstractShellContainerBlock;
import com.breakinblocks.neosync.common.block.SyncBlocks;
import com.breakinblocks.neosync.common.block.entity.ShellStorageBlockEntity;

@OnlyIn(Dist.CLIENT)
public class ZeroPointShellStorageBlockEntityRenderer extends ShellStorageBlockEntityRenderer {
    private static final ResourceLocation ZERO_POINT_SHELL_STORAGE_TEXTURE_ID = ResourceLocation.fromNamespaceAndPath(NeoSync.MOD_ID, "textures/block/zero_point_shell_storage.png");
    private static final BlockState DEFAULT_STATE = SyncBlocks.ZERO_POINT_SHELL_STORAGE.get().defaultBlockState()
            .setValue(AbstractShellContainerBlock.HALF, DoubleBlockHalf.LOWER)
            .setValue(AbstractShellContainerBlock.FACING, Direction.SOUTH)
            .setValue(AbstractShellContainerBlock.OPEN, false);

    public ZeroPointShellStorageBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected BlockState getDefaultState() {
        return DEFAULT_STATE;
    }

    @Override
    protected ResourceLocation getTextureId(ShellStorageBlockEntity blockEntity) {
        DyeColor color = blockEntity.getColor();
        if (color == null || color == DyeColor.CYAN) {
            return ZERO_POINT_SHELL_STORAGE_TEXTURE_ID;
        }

        return STORAGE_TRIM_TEXTURES.getTexture(color, ZERO_POINT_SHELL_STORAGE_TEXTURE_ID);
    }
}
