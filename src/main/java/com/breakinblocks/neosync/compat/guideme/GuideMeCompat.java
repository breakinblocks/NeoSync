package com.breakinblocks.neosync.compat.guideme;

import com.breakinblocks.neosync.NeoSync;
import guideme.Guides;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public final class GuideMeCompat {
    public static final ResourceLocation GUIDE_ID = NeoSync.locate("guide");
    private static final ResourceLocation GUIDE_ID_COMPONENT = ResourceLocation.fromNamespaceAndPath("guideme", "guide_id");
    private static final boolean LOADED = ModList.get().isLoaded("guideme");

    private GuideMeCompat() {}

    public static boolean isLoaded() {
        return LOADED;
    }

    public static ItemStack createGuideItem() {
        return Guides.createGuideItem(GUIDE_ID);
    }

    @Nullable
    public static Object getGuideId(ItemStack stack) {
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(GUIDE_ID_COMPONENT);
        return type == null ? null : stack.get(type);
    }
}
