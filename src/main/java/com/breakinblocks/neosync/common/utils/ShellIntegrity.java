package com.breakinblocks.neosync.common.utils;

import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.api.shell.ShellStateContainer;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = NeoSync.MOD_ID)
public final class ShellIntegrity {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ShellIntegrity() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            heal(player);
        }
    }

    public static Optional<ShellStateContainer> findContainer(MinecraftServer server, ShellState state) {
        ServerLevel world =
                WorldUtil.findWorld(server.getAllLevels(), state.getWorld()).orElse(null);
        if (world == null) {
            return Optional.empty();
        }
        if (state.getSubLevelUuid() == null) {
            world.getChunk(state.getPos().getX() >> 4, state.getPos().getZ() >> 4);
        }
        return Optional.ofNullable(ShellStateContainer.findAt(
                world, state.getPos(), state.getLocalOffset(), null, state.getSubLevelUuid()));
    }

    public static boolean isIntact(MinecraftServer server, ShellState state) {
        return state.isVirtual()
                || findContainer(server, state)
                        .map(container -> state.equals(container.getShellState()))
                        .orElse(Boolean.FALSE);
    }

    public static void heal(ServerPlayer player) {
        Shell shell = (Shell) player;
        List<ShellState> stored =
                shell.getAvailableShellStates().filter(x -> !x.isVirtual()).toList();
        for (ShellState state : stored) {
            ServerLevel world = WorldUtil.findWorld(player.server.getAllLevels(), state.getWorld())
                    .orElse(null);
            if (world == null) {
                continue;
            }
            ShellStateContainer container = findContainer(player.server, state).orElse(null);
            if (container != null && state.equals(container.getShellState())) {
                continue;
            }
            if (container != null && container.getShellState() == null) {
                container.setShellState(state);
                LOGGER.warn(
                        "Restored {}'s shell to the empty container at {} in {}",
                        player.getScoreboardName(),
                        state.getPos().toShortString(),
                        state.getWorld());
                continue;
            }
            shell.remove(state);
            state.drop(world);
            LOGGER.warn(
                    "Removed {}'s shell at {} in {}: {}, so its belongings were dropped there",
                    player.getScoreboardName(),
                    state.getPos().toShortString(),
                    state.getWorld(),
                    container == null ? "no shell container is there" : "the container holds another shell");
        }
    }
}
