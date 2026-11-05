package com.breakinblocks.neosync.mixins.common;

import com.breakinblocks.neosync.api.shell.Shell;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow
    public int experienceLevel;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow
    public abstract boolean isSpectator();

    @Inject(method = "getBaseExperienceReward", at = @At("RETURN"), cancellable = true)
    private void forceDropXp(CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() == 0
                && this instanceof Shell shell
                && shell.isArtificial()
                && !this.isSpectator()
                && this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            cir.setReturnValue(Math.min(this.experienceLevel * 7, 100));
        }
    }
}
