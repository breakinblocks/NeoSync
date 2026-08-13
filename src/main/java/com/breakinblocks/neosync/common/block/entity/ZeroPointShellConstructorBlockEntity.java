package com.breakinblocks.neosync.common.block.entity;

import com.breakinblocks.neosync.api.shell.ShellState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ZeroPointShellConstructorBlockEntity extends ShellConstructorBlockEntity {
    public ZeroPointShellConstructorBlockEntity(BlockPos pos, BlockState state) {
        super(SyncBlockEntities.ZERO_POINT_SHELL_CONSTRUCTOR.get(), pos, state);
        this.color = DyeColor.CYAN;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if (this.color == null) {
            this.color = DyeColor.CYAN;
        }
    }

    @Override
    public void onServerTick(Level world, BlockPos pos, BlockState state) {
        super.onServerTick(world, pos, state);
        if (this.shell != null && this.shell.getProgress() < ShellState.PROGRESS_DONE) {
            this.shell.setProgress(ShellState.PROGRESS_DONE);
            this.setChanged();
            this.sync();
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return 0;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
