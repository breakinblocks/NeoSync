package com.breakinblocks.neosync.common.block.entity;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import com.breakinblocks.neosync.api.shell.ShellState;

public class ShellEntity extends RemotePlayer {
    public boolean isActive;
    public float pitchProgress;
    private final ShellState state;
    private Runnable onInitialized;

    public ShellEntity(ShellState state) {
        this(Minecraft.getInstance().level, state);
    }

    public ShellEntity(ClientLevel world, ShellState state) {
        super(world, buildProfile(state));
        this.isActive = false;
        this.pitchProgress = 0;
        this.state = state;
        this.snapTo(state.getPos().getX() + 0.5, state.getPos().getY(), state.getPos().getZ() + 0.5, 0F, 0F);

        if (this.onInitialized != null) {
            this.onInitialized.run();
            this.onInitialized = null;
        }
    }

    public void onInitialized(Runnable runnable) {
        if (this.state == null) {
            this.onInitialized = runnable;
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public ShellState getState() {
        return this.state;
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource damageSource) {
    }

    @Override
    public boolean isCreative() {
        return true;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public PlayerInfo getPlayerInfo() {
        return null;
    }

    private static GameProfile buildProfile(ShellState state) {
        PropertyMap props = new PropertyMap(ImmutableMultimap.of());
        String value = state.getTextureValue();
        if (value != null) {
            ImmutableMultimap.Builder<String, Property> builder = ImmutableMultimap.builder();
            builder.put("textures", new Property("textures", value, state.getTextureSignature()));
            props = new PropertyMap(builder.build());
        }
        return new GameProfile(state.getOwnerUuid(), state.getOwnerName(), props);
    }
}
