package com.breakinblocks.neosync.compat.guideme;

import com.breakinblocks.neosync.NeoSync;
import guideme.Guides;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public final class GuideMeCompat {
    public static final Identifier GUIDE_ID = NeoSync.locate("guide");
    private static final Identifier GUIDE_ITEM = Identifier.fromNamespaceAndPath("guideme", "guide");
    private static final Identifier GUIDE_ID_COMPONENT = Identifier.fromNamespaceAndPath("guideme", "guide_id");
    private static final boolean LOADED = ModList.get().isLoaded("guideme");

    private GuideMeCompat() {}

    public static boolean isLoaded() {
        return LOADED;
    }

    public static ItemStack createGuideItem() {
        return Guides.createGuideItem(GUIDE_ID);
    }

    @SuppressWarnings("unchecked")
    public static ItemStackTemplate createGuideTemplate() {
        Item item = BuiltInRegistries.ITEM.getValue(GUIDE_ITEM);
        DataComponentType<Identifier> type = (DataComponentType<Identifier>) guideIdComponent();
        return new ItemStackTemplate(item, DataComponentPatch.builder().set(type, GUIDE_ID).build());
    }

    @Nullable
    public static DataComponentType<?> guideIdComponent() {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(GUIDE_ID_COMPONENT);
    }
}
