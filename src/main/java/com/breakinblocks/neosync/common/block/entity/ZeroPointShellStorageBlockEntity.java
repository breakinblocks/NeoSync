package com.breakinblocks.neosync.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ZeroPointShellStorageBlockEntity extends ShellStorageBlockEntity {
    private static final double DOOR_ACTIVATION_RANGE = 3.5;

    public ZeroPointShellStorageBlockEntity(BlockPos pos, BlockState state) {
        super(SyncBlockEntities.ZERO_POINT_SHELL_STORAGE.get(), pos, state);
    }

    @Override
    protected boolean requiresPower() {
        return false;
    }

    @Override
    protected boolean shouldOpenDoors(Level world, BlockPos pos, boolean isPowered) {
        return isPowered && world.hasNearbyAlivePlayer(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, DOOR_ACTIVATION_RANGE);
    }
}
