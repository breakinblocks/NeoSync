package com.breakinblocks.neosync.compat.thirst;

import com.breakinblocks.neosync.api.shell.ShellStateComponent;
import com.breakinblocks.neosync.api.shell.ShellStateComponentFactoryRegistry;
import com.thirstwastaken2.data.ThirstData;
import com.thirstwastaken2.data.ThirstManager;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class ThirstShellStateComponentFactory implements ShellStateComponentFactoryRegistry.ShellStateComponentFactory {
    @Override
    public ShellStateComponent empty() {
        return new StoredThirst();
    }

    @Override
    public ShellStateComponent of(ServerPlayer player) {
        return new BodyThirst(player);
    }

    private static final class StoredThirst extends ThirstShellStateComponent {
        private Levels levels;

        @Override
        @Nullable
        protected Levels getLevels() {
            return this.levels;
        }

        @Override
        protected void setLevels(@Nullable Levels levels) {
            this.levels = levels;
        }
    }

    private static final class BodyThirst extends ThirstShellStateComponent {
        private final ServerPlayer player;

        private BodyThirst(ServerPlayer player) {
            this.player = player;
        }

        @Override
        protected Levels getLevels() {
            ThirstData data = ThirstManager.get(this.player);
            return new Levels(data.thirst(), data.quenched(), data.exhaustion());
        }

        @Override
        protected void setLevels(@Nullable Levels levels) {
            ThirstData full = ThirstData.full();
            Levels target = levels != null ? levels : new Levels(full.thirst(), full.quenched(), full.exhaustion());
            boolean enabled = ThirstManager.get(this.player).enabled();
            ThirstManager.set(this.player, new ThirstData(target.thirst(), target.quenched(), target.exhaustion(), enabled));
        }
    }
}
