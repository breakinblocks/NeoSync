package com.breakinblocks.neosync.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neosync.NeoSync;
import com.breakinblocks.neosync.common.item.SyncItems;
import com.breakinblocks.neosync.compat.guideme.GuideMeCompat;

import java.util.List;

@JeiPlugin
public class SyncJeiPlugin implements IModPlugin {
    private static final Identifier ID = NeoSync.locate("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        if (!GuideMeCompat.isLoaded()) {
            return;
        }
        DataComponentType<?> guideId = GuideMeCompat.guideIdComponent();
        if (guideId != null) {
            registration.registerFromDataComponentTypes(GuideMeCompat.createGuideItem().getItem(), guideId);
        }
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        if (!GuideMeCompat.isLoaded()) {
            return;
        }
        registration.addAliases(VanillaTypes.ITEM_STACK, GuideMeCompat.createGuideItem(), List.of("neosync", "neosync guide", "guide book", "manual"));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new TreadmillEnergyCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(new ItemStack(SyncItems.SYNC_CORE.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.neosync.info.sync_core"));

        registration.addIngredientInfo(new ItemStack(SyncItems.SHELL_CONSTRUCTOR.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.neosync.info.shell_constructor"));

        registration.addIngredientInfo(new ItemStack(SyncItems.SHELL_STORAGE.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.neosync.info.shell_storage"));

        registration.addIngredientInfo(new ItemStack(SyncItems.ZERO_POINT_SHELL_CONSTRUCTOR.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.neosync.info.zero_point_shell_constructor"));

        registration.addIngredientInfo(new ItemStack(SyncItems.ZERO_POINT_SHELL_STORAGE.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.neosync.info.zero_point_shell_storage"));
        registration.addIngredientInfo(new ItemStack(SyncItems.MANUAL_SHELL_STORAGE.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.neosync.info.manual_shell_storage"));

        registration.addIngredientInfo(new ItemStack(SyncItems.TREADMILL.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei.neosync.info.treadmill"));

        if (GuideMeCompat.isLoaded()) {
            registration.addIngredientInfo(GuideMeCompat.createGuideItem(), VanillaTypes.ITEM_STACK,
                    Component.translatable("jei.neosync.info.guide"));
        }

        registration.addRecipes(TreadmillEnergyCategory.RECIPE_TYPE, TreadmillEnergyCategory.buildRecipes());
    }
}
