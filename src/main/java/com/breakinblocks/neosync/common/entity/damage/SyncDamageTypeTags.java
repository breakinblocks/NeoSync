package com.breakinblocks.neosync.common.entity.damage;

import com.breakinblocks.neosync.NeoSync;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public final class SyncDamageTypeTags {
    public static final TagKey<DamageType> NO_DEATH_SYNC =
            TagKey.create(Registries.DAMAGE_TYPE, NeoSync.locate("no_death_sync"));

    private SyncDamageTypeTags() {}
}
