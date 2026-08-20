package com.breakinblocks.neosync.mixins.common;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.minecraft.world.scores.Team;
import com.breakinblocks.neosync.api.SyncTeleport;
import com.breakinblocks.neosync.api.event.PlayerSyncEvents;
import com.breakinblocks.neosync.api.networking.PlayerIsAlivePacket;
import com.breakinblocks.neosync.api.networking.ShellStateUpdatePacket;
import com.breakinblocks.neosync.api.networking.ShellUpdatePacket;
import com.breakinblocks.neosync.api.networking.SynchronizationResponsePacket;
import com.breakinblocks.neosync.api.shell.*;
import com.breakinblocks.neosync.common.entity.KillableEntity;
import com.breakinblocks.neosync.common.entity.ShellArrival;
import com.breakinblocks.neosync.common.utils.BlockPosUtil;
import com.breakinblocks.neosync.common.utils.WorldUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
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

    @Unique
    private static final Logger SYNC_LOGGER = LogUtils.getLogger();

    @Unique
    private boolean isArtificial = false;

    @Unique
    private boolean shellDirty = false;

    @Unique
    private boolean undead = false;

    @Unique
    private UUID pendingSyncTarget = null;

    @Unique
    private ConcurrentMap<UUID, ShellState> shellsById = new ConcurrentHashMap<>();

    @Unique
    private Map<UUID, Tuple<ShellStateUpdateType, ShellState>> shellStateChanges = new ConcurrentHashMap<>();

    private ServerPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public UUID getShellOwnerUuid() {
        return this.getGameProfile().getId();
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
        BlockPos currentPos = this.blockPosition();

        if (this.pendingSyncTarget != null) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.OTHER_PROBLEM);
        }

        if (!this.canBeApplied(state) || state.getProgress() < ShellState.PROGRESS_DONE) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_SHELL);
        }

        UUID requestedUuid = state.getUuid();
        boolean isDead = this.isDeadOrDying();
        ShellStateContainer currentShellContainer = isDead ? null : ShellStateContainer.findNear(player);
        if (currentShellContainer != null && currentShellContainer.getShellState() != null) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_CURRENT_LOCATION);
        }

        PlayerSyncEvents.ShellSelectionFailureReason selectionFailureReason = PlayerSyncEvents.ALLOW_SHELL_SELECTION.invoker().allowShellSelection(player, currentShellContainer);
        if (selectionFailureReason != null) {
            return Either.right(selectionFailureReason::toText);
        }

        ServerLevel targetWorld = WorldUtil.findWorld(this.server.getAllLevels(), state.getWorld()).orElse(null);
        if (targetWorld == null) {
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION);
        }

        ShellStateContainer targetShellContainer = this.findTargetContainer(targetWorld, state);
        if (!state.isVirtual()) {
            if (targetShellContainer == null) {
                return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION);
            }
            state = targetShellContainer.getShellState();
        }
        PlayerSyncEvents.SyncFailureReason finalFailureReason = this.canBeApplied(state) ? PlayerSyncEvents.ALLOW_SYNCING.invoker().allowSync(this, state) : PlayerSyncEvents.SyncFailureReason.INVALID_SHELL;
        if (finalFailureReason != null) {
            return Either.right(finalFailureReason);
        }

        PlayerSyncEvents.START_SYNCING.invoker().onStartSyncing(this, state);

        if (isDead && !this.undead) {
            this.pendingSyncTarget = requestedUuid;
            this.sendEmptyDeathMessageInChat();
            return Either.right(PlayerSyncEvents.SyncFailureReason.OTHER_PROBLEM);
        }

        if (!isDead && currentShellContainer == null) {
            this.pendingSyncTarget = requestedUuid;
            this.hurt(this.damageSources().genericKill(), Float.MAX_VALUE);
            if (!this.isDeadOrDying()) {
                this.pendingSyncTarget = null;
                return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_CURRENT_LOCATION);
            }
            return Either.right(PlayerSyncEvents.SyncFailureReason.OTHER_PROBLEM);
        }

        ShellState storedState = null;
        if (currentShellContainer != null) {
            storedState = ShellState.of(player, currentPos, currentShellContainer.getColor());
            currentShellContainer.setShellState(storedState);
            if (currentShellContainer.isRemotelyAccessible()) {
                this.add(storedState);
            }
        }

        if (!this.moveInto(state, targetShellContainer)) {
            if (currentShellContainer != null) {
                if (storedState != null && currentShellContainer.isRemotelyAccessible()) {
                    this.remove(storedState);
                }
                currentShellContainer.setShellState(null);
            }
            return Either.right(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION);
        }

        PlayerSyncEvents.STOP_SYNCING.invoker().onStopSyncing(player, currentPos, storedState);
        return Either.left(storedState);
    }

    @Override
    public UUID getPendingSyncTarget() {
        return this.pendingSyncTarget;
    }

    @Unique
    private ShellStateContainer findTargetContainer(ServerLevel targetWorld, ShellState state) {
        if (state.isVirtual()) {
            return null;
        }

        BlockPos targetPos = state.getPos();
        if (state.getSubLevelUuid() == null) {
            targetWorld.getChunk(targetPos.getX() >> 4, targetPos.getZ() >> 4);
        }
        return ShellStateContainer.findAt(targetWorld, targetPos, state.getLocalOffset(), (ServerPlayer)(Object)this, state.getSubLevelUuid());
    }

    @Unique
    private boolean moveInto(ShellState state, ShellStateContainer targetShellContainer) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        if (targetShellContainer == null) {
            if (!this.tryApply(ShellState.anchor(player, state.getWorld(), state.getPos()))) {
                return false;
            }
            if (state.isTemporary()) {
                this.remove(state);
            }
            return true;
        }
        if (!this.tryApply(state)) {
            return false;
        }
        targetShellContainer.setShellState(null);
        this.remove(state);
        return true;
    }

    @Unique
    private void completePendingSync() {
        ServerPlayer player = (ServerPlayer)(Object)this;
        ShellState state = this.shellsById.get(this.pendingSyncTarget);
        this.pendingSyncTarget = null;

        BlockPos previousPos = this.blockPosition();
        ResourceLocation previousWorldId = WorldUtil.getId(this.level());
        Direction previousFacing = BlockPosUtil.getHorizontalFacing(previousPos, this.level()).orElse(player.getDirection().getOpposite());

        ServerLevel targetWorld = state == null ? null : WorldUtil.findWorld(this.server.getAllLevels(), state.getWorld()).orElse(null);
        if (targetWorld == null) {
            this.failPendingSync(previousWorldId, previousPos, previousFacing);
            return;
        }

        ShellStateContainer targetShellContainer = this.findTargetContainer(targetWorld, state);
        if (!state.isVirtual()) {
            if (targetShellContainer == null || !this.canBeApplied(targetShellContainer.getShellState())) {
                this.failPendingSync(previousWorldId, previousPos, previousFacing);
                return;
            }
            state = targetShellContainer.getShellState();
        }

        if (!this.moveInto(state, targetShellContainer)) {
            this.failPendingSync(previousWorldId, previousPos, previousFacing);
            return;
        }

        new SynchronizationResponsePacket(
                previousWorldId, previousPos, previousFacing,
                WorldUtil.getId(this.level()), this.blockPosition(), player.getDirection().getOpposite(),
                Optional.empty()).send(player);

        PlayerSyncEvents.STOP_SYNCING.invoker().onStopSyncing(player, previousPos, null);
    }

    @Unique
    private void failPendingSync(ResourceLocation worldId, BlockPos pos, Direction facing) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        player.sendSystemMessage(PlayerSyncEvents.SyncFailureReason.INVALID_TARGET_LOCATION.toText());
        new SynchronizationResponsePacket(worldId, pos, facing, worldId, pos, facing, Optional.empty()).send(player);
    }

    @Override
    public void apply(ShellState state) {
        this.tryApply(state);
    }

    @Unique
    private boolean tryApply(ShellState state) {
        Objects.requireNonNull(state);

        ServerPlayer serverPlayer = (ServerPlayer)(Object)this;
        MinecraftServer server = Objects.requireNonNull(this.getServer());
        ServerLevel targetWorld = WorldUtil.findWorld(server.getAllLevels(), state.getWorld()).orElse(null);
        if (targetWorld == null) {
            SYNC_LOGGER.warn("Sync target world {} does not exist; leaving {} untouched", state.getWorld(), this.getName().getString());
            return false;
        }

        this.stopRiding();
        this.removeEntitiesOnShoulder();
        this.clearFire();
        this.setTicksFrozen(0);
        this.setRemainingFireTicks(0);
        this.removeAllEffects();

        new PlayerIsAlivePacket(serverPlayer).sendToAll(server);
        SyncTeleport.begin();
        try {
            if (!this.teleport(targetWorld, state)) {
                SYNC_LOGGER.warn("Sync teleport to {} was refused; leaving {} untouched", state.getWorld(), this.getName().getString());
                return false;
            }
        } finally {
            SyncTeleport.end();
        }
        this.isArtificial = state.isArtificial();

        Inventory inventory = this.getInventory();
        int selectedSlot = inventory.selected;
        state.getInventory().copyTo(inventory);
        inventory.selected = selectedSlot;

        ShellStateComponent playerComponent = ShellStateComponent.of(serverPlayer);
        playerComponent.clone(state.getComponent());

        serverPlayer.setGameMode(GameType.byId(state.getGameMode()));
        this.setHealth(state.getHealth());
        this.experienceLevel = state.getExperienceLevel();
        this.experienceProgress = state.getExperienceProgress();
        this.totalExperience = state.getTotalExperience();
        this.getFoodData().setFoodLevel(state.getFoodLevel());
        this.getFoodData().setSaturation(state.getSaturationLevel());
        this.getFoodData().setExhaustion(state.getExhaustion());

        this.undead = false;
        this.dead = false;
        this.deathTime = 0;
        this.fallDistance = 0;
        this.lastSentExp = -1;
        this.lastSentHealth = -1;
        this.lastSentFood = -1;
        this.shellDirty = true;
        return true;
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
        if (state == null) {
            return;
        }

        if (this.shellsById.remove(state.getUuid()) != null) {
            this.shellStateChanges.put(state.getUuid(), new Tuple<>(ShellStateUpdateType.REMOVE, state));
        }
    }

    @Override
    public void update(ShellState state) {
        if (state == null) {
            return;
        }

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

        if (this.pendingSyncTarget != null && !this.isRemoved()) {
            this.completePendingSync();
        }

        if (this.shellDirty) {
            this.shellDirty = false;
            this.shellStateChanges.clear();
            new ShellUpdatePacket(WorldUtil.getId(this.level()), this.isArtificial, this.shellsById.values()).send(player);
        }

        for (Tuple<ShellStateUpdateType, ShellState> upd : this.shellStateChanges.values()) {
            new ShellStateUpdatePacket(upd.getA(), upd.getB()).send(player);
        }
        this.shellStateChanges.clear();
    }

    @ModifyExpressionValue(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z", ordinal = 0))
    private boolean hidePendingSyncDeathMessage(boolean showDeathMessages) {
        return showDeathMessages && this.pendingSyncTarget == null;
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource source, CallbackInfo ci) {
        if (!this.isArtificial || this.pendingSyncTarget != null) {
            return;
        }

        ShellState respawnShell = this.shellsById.values().stream().filter(x -> this.canBeApplied(x) && x.getProgress() >= ShellState.PROGRESS_DONE).findAny().orElse(null);
        if (respawnShell == null) {
            return;
        }

        this.gameEvent(GameEvent.ENTITY_DIE);
        if (CommonHooks.onLivingDeath(this, source)) {
            ci.cancel();
            return;
        }

        if (this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) {
            this.sendDeathMessageInChat();
        } else {
            this.sendEmptyDeathMessageInChat();
        }

        this.removeEntitiesOnShoulder();
        if (this.level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            this.tellNeutralMobsThatIDied();
        }

        if (!this.isSpectator() && this.level() instanceof ServerLevel serverLevel) {
            this.dropAllDeathLoot(serverLevel, source);
            if (this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                this.destroyVanishingCursedItems();
                this.getInventory().dropAll();
            }
        }

        this.undead = true;
        ci.cancel();
    }

    @Override
    public boolean updateKillableEntityPostDeath() {
        this.deathTime = Mth.clamp(++this.deathTime, 0, 20);
        if (this.pendingSyncTarget != null) {
            return true;
        }

        if (this.isArtificial && this.shellsById.values().stream().anyMatch(x -> this.canBeApplied(x) && x.getProgress() >= ShellState.PROGRESS_DONE)) {
            return true;
        }

        if (this.undead) {
            this.die(level().damageSources().magic());
            this.undead = false;
        }

        if (this.deathTime == 20) {
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(RemovalReason.KILLED);
        }
        return true;
    }

    @Unique
    private void sendDeathMessageInChat() {
        Component text = this.getCombatTracker().getDeathMessage();
        this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), text));
        Team team = this.getTeam();
        if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
            if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                this.server.getPlayerList().broadcastSystemToTeam(this, text);
            } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                this.server.getPlayerList().broadcastSystemToAllExceptTeam(this, text);
            }
        } else {
            this.server.getPlayerList().broadcastSystemMessage(text, false);
        }
    }

    @Unique
    private void sendEmptyDeathMessageInChat() {
        this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), Component.empty()));
    }

    @Shadow
    protected abstract void tellNeutralMobsThatIDied();

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomDataToNbt(CompoundTag nbt, CallbackInfo ci) {
        ListTag shellList = new ListTag();
        this.shellsById
                .values()
                .stream()
                .map(x -> x.writeNbt(new CompoundTag()))
                .forEach(shellList::add);

        nbt.putBoolean("IsArtificial", this.isArtificial);
        nbt.put("Shells", shellList);
        if (this.pendingSyncTarget != null) {
            nbt.putUUID("PendingSyncTarget", this.pendingSyncTarget);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCustomDataFromNbt(CompoundTag nbt, CallbackInfo ci) {
        this.isArtificial = nbt.getBoolean("IsArtificial");
        this.pendingSyncTarget = nbt.hasUUID("PendingSyncTarget") ? nbt.getUUID("PendingSyncTarget") : null;
        this.shellsById = nbt.getList("Shells", Tag.TAG_COMPOUND)
                .stream()
                .map(x -> ShellState.fromNbt((CompoundTag)x))
                .collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));

        Collection<Tuple<ShellStateUpdateType, ShellState>> updates = ((ShellStateManager)this.server).popPendingUpdates(this.uuid);
        for (Tuple<ShellStateUpdateType, ShellState> update : updates) {
            ShellState state = update.getB();
            switch (update.getA()) {
                case ADD, UPDATE -> {
                    if (this.uuid.equals(state.getOwnerUuid())) {
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
        this.pendingSyncTarget = ((ServerShell)oldPlayer).getPendingSyncTarget();
        this.shellsById = shell.getAvailableShellStates().collect(Collectors.toConcurrentMap(ShellState::getUuid, x -> x));
        this.shellStateChanges = new HashMap<>();
        this.shellDirty = true;
    }

    @Inject(method = "setServerLevel", at = @At("HEAD"))
    private void setWorld(ServerLevel world, CallbackInfo ci) {
        if (world != this.level()) {
            this.shellDirty = true;
        }
    }

    @Unique
    private boolean teleport(ServerLevel targetWorld, ShellState state) {
        ServerPlayer serverPlayer = (ServerPlayer)(Object)this;
        Vec3 target = state.resolveWorldPos(targetWorld);
        float yaw;
        if (state.getSubLevelUuid() != null) {
            yaw = state.resolveYaw(targetWorld, serverPlayer.getYRot());
        } else {
            BlockPos pos = state.getPos();
            LevelChunk chunk = targetWorld.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            yaw = BlockPosUtil.getHorizontalFacing(pos, chunk).map(d -> d.getOpposite().toYRot()).orElse(0F);
        }

        serverPlayer.teleportTo(targetWorld, target.x, target.y, target.z, Collections.emptySet(), yaw, 0F);
        if (serverPlayer.level() != targetWorld) {
            return false;
        }

        serverPlayer.setDeltaMovement(Vec3.ZERO);
        serverPlayer.hurtMarked = true;
        serverPlayer.fallDistance = 0F;

        if (state.getSubLevelUuid() != null) {
            ShellArrival.schedule(serverPlayer, targetWorld, state);
        }
        return true;
    }
}