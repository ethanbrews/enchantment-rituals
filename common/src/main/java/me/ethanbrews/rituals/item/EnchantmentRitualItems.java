package me.ethanbrews.rituals.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.ethanbrews.rituals.EnchantmentRituals;
import me.ethanbrews.rituals.block.EnchantmentRitualBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EnchantmentRitualItems {

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(EnchantmentRituals.MOD_ID, Registries.ITEM);
    public static final RegistrySupplier<BlockItem> ENCHANTMENT_PEDESTAL_BLOCK_ITEM =
            ITEMS.register("enchant_pedestal", () ->
                    new BlockItem(EnchantmentRitualBlocks.ENCHANTMENT_PEDESTAL_BLOCK.get(), getDefaultItemProperties())
            );
    public static final RegistrySupplier<BlockItem> BRAIN_IN_A_JAR_BLOCK_ITEM =
            ITEMS.register("brain_in_a_jar", () ->
                    new BlockItem(EnchantmentRitualBlocks.BRAIN_IN_A_JAR_BLOCK.get(), getDefaultItemProperties())
            );
    public static final RegistrySupplier<BlockItem> GLASS_JAR_BLOCK_ITEM =
            ITEMS.register("glass_jar", () ->
                    new BlockItem(EnchantmentRitualBlocks.GLASS_JAR_BLOCK.get(), getDefaultItemProperties())
            );
    public static final RegistrySupplier<BrainItem> BRAIN_ITEM =
            ITEMS.register("brain", BrainItem::new);

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(EnchantmentRituals.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MOD_TAB = TABS.register(
            "general",
            () -> CreativeTabRegistry.create(
                    Component.translatable("category."+EnchantmentRituals.MOD_ID+".general"),
                    () -> new ItemStack(ENCHANTMENT_PEDESTAL_BLOCK_ITEM.get())
            )
    );

    public static Item.Properties getDefaultItemProperties() {
        return new Item.Properties().arch$tab(MOD_TAB.get());
    }

    public static void register() {
        TABS.register();
        ITEMS.register();
    }
}
