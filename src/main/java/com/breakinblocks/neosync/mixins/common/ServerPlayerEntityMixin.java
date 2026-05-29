package com.breakinblocks.neosync.mixins.common;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.breakinblocks.neosync.api.event.PlayerSyncEvents;
import com.breakinblocks.neosync.api.networking.PlayerIsAlivePacket;
import com.breakinblocks.neosync.api.networking.ShellStateUpdatePacket;
import com.breakinblocks.neosync.api.networking.ShellUpdatePacket;
import com.breakinblocks.neosync.api.shell.Shell;
import com.breakinblocks.neosync.api.shell.ServerShell;
import com.breakinblocks.neosync.api.shell.ShellState;
import com.breakinblocks.neosync.api.shell.ShellStateComponent;
import com.breakinblocks.neosync.api.shell.ShellStateContainer;
import com.breakinblocks.neosync.api.shell.ShellStateManager;
import com.breakinblocks.neosync.api.shell.ShellStateUpdateType;
import com.breakinblocks.neosync.common.entity.KillableEntity;
import com.breakinblocks.neosync.common.utils.WorldUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerEntityMixin extends Player implements ServerShell, KillableEntity {
    @Shadow
    private int lastSentExp;

    @Shadow
    private float lastSentHealth;

    @Shadow
    private int lastSentFood;

    @Shadow @Final
    public MinecraftServer server;

    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Shadow
    protected abstract void removeEntitiesOnShoulder();

    private ServerPlayerEntityMixin(Level level, GameProfile profile) {
        super(level, profile);
    }

    @Unique
    private boolean isArtificial = false;

    @Unique
    private boolean shellDirty = false;

    @Unique
    private boolean undead = false;

    @Unique
    private ConcurrentMap<UUID, ShellState> shellsById = new ConcurrentHashMap<>();

    @Unique
    private Map<UUID, Tuple<ShellStateUpdateType, ShellState>> shellStateChanges = new ConcurrentHashMap<>();

    @Shadow
    protected abstract void tellNeutralMobsThatIDied();

    @Override
    public UUID getShellOwnerUuid() {
        return ((ServerPlayer) (Object) this).getGameProfile().id();
    }

    @Override
    public boolean isArtificial() {
        return this.isArtificial;
    }

    @Override
    public void changeArtificialStatus(boolean isArtificial) {
        if (this.isArtificial != isArtificial) {
            this.isArtificial = isArtificial;
            this.shellDirty = true;
        }
    }

    @Override
    public Either<ShellState, PlayerSyncEvents.SyncFailureReason> sync(ShellState state) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        BlockPos currentPos = player.blockPosition();
        Level currentWorld = player.level();

        if (!this.canBeApplied(state) || state.getProgress() < ShellState.PROGRESS_DONE) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_SHELL);
        }

        boolean isDead = player.isDeadOrDying();
        if (isDead && !this.undead) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_CURRENT_LOCATION);
        }
        ShellStateContainer currentShellContainer = isDead ? null : ShellStateContainer.find(currentWorld, currentPos);
        if (!isDead && (currentShellContainer == null || currentShellContainer.getShellState() != null)) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_CURRENT_LOCATION);
        }

        PlayerSyncEvents.ShellSelectionFailureReason selectionFailureReason = PlayerSyncEvents.ALLOW_SHELL_SELECTION.invoker().allowShellSelection(player, currentShellContainer);
        if (selectionFailureReason != null) {
            return Either.right(selectionFailureReason::toText);
        }

        Identifier targetWorldId = state.getWorld();
        ServerLevel targetWorld = WorldUtil.findWorld(this.server.getAllLevels(), targetWorldId).orElse(null);
        if (targetWorld == null) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION);
        }

        BlockPos targetPos = state.getPos();
        LevelChunk targetChunk = targetWorld.getChunk(targetPos.getX() >> 4, targetPos.getZ() >> 4);
        ShellStateContainer targetShellContainer = targetChunk == null ? null : ShellStateContainer.find(targetWorld, targetPos);
        if (targetShellContainer == null) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION);
        }

        state = targetShellContainer.getShellState();
        PlayerSyncEvents.SyncFailureReason finalFailureReason = this.canBeApplied(state) ? PlayerSyncEvents.ALLOW_SYNCING.invoker().allowSync(this, state) : PlayerSyncEvents.SyncFailureReason.INVALID_SHELL;
        if (finalFailureReason != null) {
            return Either.right(finalFailureReason);
        }

        PlayerSyncEvents.START_SYNCING.invoker().onStartSyncing(this, state);

        ShellState storedState = null;
        if (currentShellContainer != null) {
            storedState = ShellState.of(player, currentPos, currentShellContainer.getColor());
            currentShellContainer.setShellState(storedState);
            if (currentShellContainer.isRemotelyAccessible()) {
                this.add(storedState);
            }
        }

        targetShellContainer.setShellState(null);
        this.remove(state);
        this.apply(state);

        PlayerSyncEvents.STOP_SYNCING.invoker().onStopSyncing(player, currentPos, storedState);
        return Either.left(storedState);
    }

    @Override
    public void apply(ShellState state) {
        Objects.requireNonNull(state);

        ServerPlayer serverPlayer = (ServerPlayer)(Object)this;
        MinecraftServer server = Objects.requireNonNull(serverPlayer.level().getServer());
        ServerLevel targetWorld = WorldUtil.findWorld(server.getAllLevels(), state.getWorld()).orElse(null);
        if (targetWorld == null) {
            return;
        }

        this.stopRiding();
        this.removeEntitiesOnShoulder();
        this.clearFire();
        this.setTicksFrozen(0);
        this.setRemainingFireTicks(0);
        this.removeAllEffects();

        new PlayerIsAlivePacket(serverPlayer).sendToAll(server);
        this.isArtificial = state.isArtificial();

        Inventory inventory = serverPlayer.getInventory();
        int selectedSlot = inventory.getSelectedSlot();
        state.getInventory().copyTo(inventory);
        inventory.setSelectedSlot(selectedSlot);

        ShellStateComponent playerComponent = ShellStateComponent.of(serverPlayer);
        playerComponent.clone(state.getComponent());

        serverPlayer.setGameMode(GameType.byId(state.getGameMode()));
        serverPlayer.setHealth(state.getHealth());
        serverPlayer.experienceLevel = state.getExperienceLevel();
        serverPlayer.experienceProgress = state.getExperienceProgress();
        serverPlayer.totalExperience = state.getTotalExperience();
        serverPlayer.getFoodData().setFoodLevel(state.getFoodLevel());
        serverPlayer.getFoodData().setSaturation(state.getSaturationLevel());

        this.undead = false;
        this.dead = false;
        this.deathTime = 0;
        this.fallDistance = 0;
        this.lastSentExp = -1;
        this.lastSentHealth = -1;
        this.lastSentFood = -1;
        this.shellDirty = true;
    }

    @Override
    public Stream<ShellState> getAvailableShellStates() {
        return this.shellsById.values().stream();
    }

    @Override
    public void setAvailableShellStates(Stream<ShellState> states) {
        this.shellsById = states.collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));
        this.shellDirty = true;
    }

    @Override
    public ShellState getShellStateByUuid(UUID uuid) {
        return uuid == null ? null : this.shellsById.get(uuid);
    }

    @Override
    public void add(ShellState state) {
        if (!this.canBeApplied(state)) {
            return;
        }
        this.shellsById.put(state.getUuid(), state);
        this.shellStateChanges.put(state.getUuid(), new Tuple<>(ShellStateUpdateType.ADD, state));
    }

    @Override
    public void remove(ShellState state) {
        if (state == null) return;
        if (this.shellsById.remove(state.getUuid()) != null) {
            this.shellStateChanges.put(state.getUuid(), new Tuple<>(ShellStateUpdateType.REMOVE, state));
        }
    }

    @Override
    public void update(ShellState state) {
        if (state == null) return;
        boolean updated;
        if (this.canBeApplied(state)) {
            updated = this.shellsById.put(state.getUuid(), state) != null;
        } else {
            updated = this.shellsById.computeIfPresent(state.getUuid(), (a, b) -> state) != null;
        }
        this.shellStateChanges.put(state.getUuid(), new Tuple<>(updated ? ShellStateUpdateType.UPDATE : ShellStateUpdateType.ADD, state));
    }

    @Inject(method = "doTick", at = @At("HEAD"))
    private void playerTick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;

        if (this.shellDirty) {
            this.shellDirty = false;
            this.shellStateChanges.clear();
            new ShellUpdatePacket(WorldUtil.getId(player.level()), this.isArtificial, this.shellsById.values()).send(player);
        }

        for (Tuple<ShellStateUpdateType, ShellState> upd : this.shellStateChanges.values()) {
            new ShellStateUpdatePacket(upd.getA(), upd.getB()).send(player);
        }
        this.shellStateChanges.clear();
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource source, CallbackInfo ci) {
        if (!this.isArtificial) {
            return;
        }

        ServerPlayer player = (ServerPlayer)(Object)this;
        ShellState respawnShell = this.shellsById.values().stream()
                .filter(x -> this.canBeApplied(x) && x.getProgress() >= ShellState.PROGRESS_DONE)
                .findAny().orElse(null);
        if (respawnShell == null) {
            return;
        }

        sendDeathMessageInChat(player, source);

        this.removeEntitiesOnShoulder();
        this.tellNeutralMobsThatIDied();

        if (!this.isSpectator()) {
            this.dropAllDeathLoot(player.level(), source);
        }

        this.undead = true;
        ci.cancel();
    }

    @Override
    public boolean updateKillableEntityPostDeath() {
        ServerPlayer player = (ServerPlayer)(Object)this;
        player.deathTime = Mth.clamp(++player.deathTime, 0, 20);
        if (this.isArtificial && this.shellsById.values().stream().anyMatch(x -> this.canBeApplied(x) && x.getProgress() >= ShellState.PROGRESS_DONE)) {
            return true;
        }

        if (this.undead) {
            player.die(player.level().damageSources().magic());
            this.undead = false;
        }

        if (player.deathTime == 20) {
            player.level().broadcastEntityEvent(player, (byte)60);
            player.remove(Entity.RemovalReason.KILLED);
        }
        return true;
    }

    @Unique
    private static void sendDeathMessageInChat(ServerPlayer player, DamageSource source) {
        Component text = player.getCombatTracker().getDeathMessage();
        player.connection.send(new ClientboundPlayerCombatKillPacket(player.getId(), text));
        Team team = player.getTeam();
        if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
            if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                player.level().getServer().getPlayerList().broadcastSystemToTeam(player, text);
            } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                player.level().getServer().getPlayerList().broadcastSystemToAllExceptTeam(player, text);
            }
        } else {
            player.level().getServer().getPlayerList().broadcastSystemMessage(text, false);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomDataToNbt(ValueOutput out, CallbackInfo ci) {
        out.putBoolean("IsArtificial", this.isArtificial);
        ValueOutput.TypedOutputList<CompoundTag> list = out.list("Shells", CompoundTag.CODEC);
        this.shellsById.values().forEach(s -> list.add(s.writeNbt(new CompoundTag())));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCustomDataFromNbt(ValueInput in, CallbackInfo ci) {
        this.isArtificial = in.getBooleanOr("IsArtificial", false);
        ValueInput.TypedInputList<CompoundTag> list = in.listOrEmpty("Shells", CompoundTag.CODEC);
        this.shellsById = list.stream()
                .map(ShellState::fromNbt)
                .collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));

        Collection<Tuple<ShellStateUpdateType, ShellState>> updates = ((ShellStateManager)this.server).popPendingUpdates(this.getShellOwnerUuid());
        for (Tuple<ShellStateUpdateType, ShellState> update : updates) {
            ShellState state = update.getB();
            switch (update.getA()) {
                case ADD, UPDATE -> {
                    if (this.getShellOwnerUuid().equals(state.getOwnerUuid())) {
                        this.shellsById.put(state.getUuid(), state);
                    }
                }
                case REMOVE -> this.shellsById.remove(state.getUuid());
            }
        }

        this.shellStateChanges = new HashMap<>();
        this.shellDirty = true;
    }

    @Inject(method = "restoreFrom", at = @At("HEAD"))
    private void copyFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        Shell shell = (Shell)oldPlayer;
        this.isArtificial = alive && shell.isArtificial();
        this.shellsById = shell.getAvailableShellStates().collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));
        this.shellStateChanges = new HashMap<>();
        this.shellDirty = true;
    }

    @Inject(method = "setServerLevel", at = @At("HEAD"))
    private void setWorld(ServerLevel world, CallbackInfo ci) {
        if (world != ((ServerPlayer)(Object)this).level()) {
            this.shellDirty = true;
        }
    }
}
