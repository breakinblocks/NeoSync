package com.breakinblocks.neosync.compat.thirst;

import com.breakinblocks.neosync.api.shell.ShellStateComponent;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public abstract class ThirstShellStateComponent extends ShellStateComponent {
    private static final String ID = "neosync:thirst";
    private static final String THIRST = "thirst";
    private static final String QUENCHED = "quenched";
    private static final String EXHAUSTION = "exhaustion";

    protected record Levels(int thirst, int quenched, float exhaustion) {}

    @Override
    public String getId() {
        return ID;
    }

    @Nullable
    protected abstract Levels getLevels();

    protected abstract void setLevels(@Nullable Levels levels);

    @Override
    public void clone(ShellStateComponent component) {
        ThirstShellStateComponent other = component.as(ThirstShellStateComponent.class);
        if (other != null) {
            this.setLevels(other.getLevels());
        }
    }

    @Override
    protected void readComponentNbt(CompoundTag nbt) {
        this.setLevels(nbt.getInt(THIRST)
                .map(thirst -> new Levels(thirst, nbt.getIntOr(QUENCHED, 0), nbt.getFloatOr(EXHAUSTION, 0.0F)))
                .orElse(null));
    }

    @Override
    protected CompoundTag writeComponentNbt(CompoundTag nbt) {
        Levels levels = this.getLevels();
        if (levels != null) {
            nbt.putInt(THIRST, levels.thirst());
            nbt.putInt(QUENCHED, levels.quenched());
            nbt.putFloat(EXHAUSTION, levels.exhaustion());
        }
        return nbt;
    }
}
