package com.breakinblocks.neosync.mixins.common;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import com.breakinblocks.neosync.common.entity.KillableEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    @Final
    private static EntityDataAccessor<Float> DATA_HEALTH_ID;

    @Inject(method = "setHealth", at = @At("RETURN"))
    private void sync$setHealth(float health, CallbackInfo ci) {
        if (health <= 0 && this instanceof KillableEntity) {
            ((KillableEntity)this).onKillableEntityDeath();
        }
    }

    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void sync$updatePostDeath(CallbackInfo ci) {
        if (this instanceof KillableEntity && ((KillableEntity)this).updateKillableEntityPostDeath()) {
            ci.cancel();
        }
    }

    @Inject(method = "onSyncedDataUpdated", at = @At("TAIL"))
    private void sync$clearDeathPose(EntityDataAccessor<?> key, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (DATA_HEALTH_ID.equals(key) && self.level().isClientSide() && self instanceof Player && self.getHealth() > 0) {
            self.deathTime = 0;
        }
    }
}