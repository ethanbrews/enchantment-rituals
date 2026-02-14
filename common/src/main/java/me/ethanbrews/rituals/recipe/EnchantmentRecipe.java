package me.ethanbrews.rituals.recipe;

import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.util.XpHelper;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record EnchantmentRecipe(
        String enchantment,
        String[][] ingredients,
        int cost,
        int level,
        float failureChance,
        int duration
) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public int getXpCost(Player player) {
        return Math.min(0, XpHelper.getLevelCostInXP(player, cost));
    }

    public boolean canPlayerAffordRecipe(Player player) {
        return cost <= 0 || XpHelper.getLevelCostInXP(player, cost) > 0;
    }

    /**
     * Converts a 2D ingredient string array to a 2D Item array.
     * Supports both direct items ("minecraft:diamond") and tags ("#minecraft:planks").
     * Tags are prefixed with '#'.
     *
     * @return 2D array of Items, or null if any slot has no valid items
     */
    public @Nullable Item[][] parseIngredients() {
        List<Item[]> result = new ArrayList<>();

        for (int slotIndex = 0; slotIndex < ingredients.length; slotIndex++) {
            String[] options = ingredients[slotIndex];
            Set<Item> validItems = new LinkedHashSet<>(); // Use Set to avoid duplicates

            for (String itemId : options) {
                try {
                    if (itemId.startsWith("#")) {
                        // It's a tag
                        ResourceLocation tagLocation = new ResourceLocation(itemId.substring(1));
                        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagLocation);

                        // Get all items in the tag
                        BuiltInRegistries.ITEM.getTag(tag).ifPresent(holders -> {
                            holders.forEach(holder -> validItems.add(holder.value()));
                        });

                        if (validItems.isEmpty()) {
                            LOGGER.error("Tag is empty or doesn't exist: {}", itemId);
                        }

                    } else {
                        // It's a direct item
                        ResourceLocation resourceLocation = new ResourceLocation(itemId);
                        Item item = BuiltInRegistries.ITEM.get(resourceLocation);

                        if (item != Items.AIR) {
                            validItems.add(item);
                        } else {
                            LOGGER.error("Unknown item: {}", itemId);
                        }
                    }
                } catch (ResourceLocationException e) {
                    LOGGER.error("Invalid resource location: {}", itemId);
                }
            }

            // If no valid items for this slot, recipe is invalid
            if (validItems.isEmpty()) {
                LOGGER.error("No valid items found for ingredient slot {}", slotIndex);
                return null;
            }

            result.add(validItems.toArray(new Item[0]));
        }

        return result.toArray(new Item[0][]);
    }

    /**
     * Parses the enchantment string to an Enchantment.
     * Supports direct enchantments ("minecraft:sharpness").
     * Tags are not supported for single enchantment results.
     * Result is cached after first parse.
     *
     * @return The Enchantment, or null if invalid
     */
    public Enchantment getEnchantment() {
        if (enchantment == null || enchantment.isEmpty()) {
            return null;
        }

        try {
            ResourceLocation resourceLocation = new ResourceLocation(enchantment);
            var ench = BuiltInRegistries.ENCHANTMENT.get(resourceLocation);

            if (ench != null) {
                return ench;
            } else {
                LOGGER.error("Unknown enchantment: {}", enchantment);
                return null;
            }
        } catch (ResourceLocationException e) {
            LOGGER.error("Invalid enchantment resource location: {}", enchantment);
            return null;
        }
    }
}


