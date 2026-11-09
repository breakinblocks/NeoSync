package com.breakinblocks.neosync.data;

import com.breakinblocks.neosync.common.block.AbstractShellContainerBlock;
import com.breakinblocks.neosync.common.block.SyncBlocks;
import com.breakinblocks.neosync.common.block.TreadmillBlock;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class SyncBlockLootSubProvider extends BlockLootSubProvider {
    public SyncBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        this.add(SyncBlocks.SHELL_STORAGE.get(), this::createShellContainerTable);
        this.add(SyncBlocks.SHELL_CONSTRUCTOR.get(), this::createShellContainerTable);
        this.add(SyncBlocks.ZERO_POINT_SHELL_STORAGE.get(), this::createShellContainerTable);
        this.add(SyncBlocks.MANUAL_SHELL_STORAGE.get(), this::createShellContainerTable);
        this.add(SyncBlocks.ZERO_POINT_SHELL_CONSTRUCTOR.get(), this::createShellContainerTable);
        this.add(
                SyncBlocks.TREADMILL.get(),
                block -> LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(block)
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(TreadmillBlock.PART, TreadmillBlock.Part.BACK))))
                                .when(ExplosionCondition.survivesExplosion())));
    }

    private LootTable.Builder createShellContainerTable(Block block) {
        return LootTable.lootTable()
                .withPool(this.applyExplosionCondition(
                        block,
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(block)
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(
                                                                AbstractShellContainerBlock.HALF,
                                                                DoubleBlockHalf.LOWER)))
                                        .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(
                                                        LootContextParams.BLOCK_ENTITY)
                                                .include(DataComponents.CUSTOM_NAME)))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Stream.of(
                        SyncBlocks.SHELL_STORAGE,
                        SyncBlocks.SHELL_CONSTRUCTOR,
                        SyncBlocks.ZERO_POINT_SHELL_STORAGE,
                        SyncBlocks.ZERO_POINT_SHELL_CONSTRUCTOR,
                        SyncBlocks.MANUAL_SHELL_STORAGE,
                        SyncBlocks.TREADMILL)
                .map(DeferredBlock::get)
                .map(b -> (Block) b)
                .toList();
    }
}
