package me.ethanbrews.rituals.recipe;

import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.util.EnchantmentHelper;
import me.ethanbrews.rituals.util.XpHelper;
import net.minecraft.ResourceLocationException;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;


public record EnchantmentRecipe(
        @Nullable RecipeInputOrOutput upgrade,
        @NotNull RecipeInputOrOutput result,
        @NotNull  IngredientSlot[] ingredients,
        @NotNull  RitualCost cost,
        @Nullable Float failureChance,
        @NotNull  String duration
) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public int getXpCost(Player player) {
        return Math.min(0, XpHelper.getLevelCostInXP(player, cost.levels()));
    }

    public boolean canPlayerAffordXpCost(Player player) {
        return cost.levels() <= 0 || getXpCost(player) > 0;
    }

    public int getTickDuration() {
        if (duration.endsWith("t")) {
            return Integer.parseInt(duration.substring(0, duration.length() - 1));
        } else if (duration.endsWith("s")) {
            int seconds = Integer.parseInt(duration.substring(0, duration.length() - 1));
            return seconds * 20; // 20 ticks per second in Minecraft
        } else {
            // Default to ticks if no suffix
            return Integer.parseInt(duration);
        }
    }

    public ItemStack applyRecipeOutput(ItemStack targetStack) throws IllegalArgumentException {
        if (!(canUpgrade(targetStack))) throw new IllegalArgumentException("Cannot upgrade " + targetStack);
        if (result.isItem()) {
            return result.getItemStack();
        }
        assert result.isEnchantment();
        assert result.getEnchantmentIdentifier() != null;
        var ench = result.getEnchantmentIdentifier().getEnchantment();
        assert ench != null;
        var level = result.getEnchantmentIdentifier().getLevel();
        assert ench.canEnchant(targetStack);
        targetStack.enchant(ench, level);
        return targetStack;
    }

    public boolean canUpgrade(ItemStack targetStack) {
        if (upgrade == null) {
            // No upgrade requirement - just check if enchantment can be applied
            if (result.isItem()) return true;

            var enchId = result.enchantment();
            assert enchId != null;
            var ench = enchId.getEnchantment();
            assert ench != null;

            // Check if the enchantment can be applied to this item
            if (!ench.canEnchant(targetStack)) {
                return false;
            }

            // Check if target already has this enchantment at a lower level
            int currentLevel = EnchantmentHelper.getLevel(ench, targetStack);
            int requiredLevel = enchId.getLevel();

            // Allow if: no enchantment yet (level 0) or current level is less than required
            return currentLevel < requiredLevel;

        } else {
            // Has upgrade requirement - check target matches the upgrade condition

            boolean itemMatches = true;
            boolean enchantmentMatches = true;

            // Check item requirement if specified
            if (upgrade.isItem()) {
                var upgradeItem = upgrade.getItemStack();
                if (upgradeItem == null) {
                    itemMatches = false;
                } else {
                    itemMatches = ItemStack.isSameItem(targetStack, upgradeItem);
                }
            }

            // Check enchantment requirement if specified
            if (upgrade.isEnchantment()) {
                var upgradeEnchId = upgrade.getEnchantmentIdentifier();
                if (upgradeEnchId == null) {
                    enchantmentMatches = false;
                } else {
                    var upgradeEnch = upgradeEnchId.getEnchantment();
                    if (upgradeEnch == null) {
                        enchantmentMatches = false;
                    } else {
                        int currentLevel = EnchantmentHelper.getLevel(upgradeEnch, targetStack);
                        int requiredLevel = upgradeEnchId.getLevel();

                        // Must have at least this level
                        enchantmentMatches = currentLevel >= requiredLevel;
                    }
                }
            }

            // Both conditions must be satisfied (if specified)
            return itemMatches && enchantmentMatches;
        }
    }

    public float getFailureChance() {
        return Objects.requireNonNullElse(failureChance, 0f);
    }

    public Item[][] getIngredients() throws ResourceLocationException {
        Item[][] result = new Item[ingredients.length][];
        List<String> emptySlots = new ArrayList<>();

        for (int i = 0; i < ingredients.length; i++) {
            try {
                result[i] = ingredients[i].getIngredients();
            } catch (ResourceLocationException e) {
                // Collect which slots failed
                emptySlots.add("slot " + i + " (" + e.getMessage() + ")");
            }
        }

        // If any slots are empty, fail the entire recipe
        if (!emptySlots.isEmpty()) {
            throw new ResourceLocationException(
                    "Recipe has empty ingredient slots: " + String.join("; ", emptySlots)
            );
        }

        return result;
    }

    public boolean isEnchantment() {
        return result.isEnchantment();
    }

    public boolean isItem() {
        return result.isItem();
    }

    public @Nullable Enchantment getEnchantment() throws IllegalStateException {
        if (!(result.isEnchantment())) {
            throw new IllegalStateException("Called getEnchantment on a recipe that produces an item.");
        }
        assert result.enchantment() != null;
        return result.enchantment().getEnchantment();
    }

    public @Nullable ItemStack getItemStack() throws IllegalStateException {
        if (!(result.isItem())) {
            throw new IllegalStateException("Called getEnchantment on a recipe that produces an item.");
        }
        return result.getItemStack();
    }

    public boolean isValidRecipe() {
        try {
            getIngredients();
        } catch (ResourceLocationException e) {
            return false;
        }

        try {
            getTickDuration();
        } catch (NumberFormatException e) {
            return false;
        }

        if (!(result.isValidOutput())) {
            return false;
        }

        if (upgrade != null && !(upgrade.isValidInput())) {
            return false;
        }

        if (isEnchantment()) {
            return getEnchantment() != null;
        } else if (isItem()) {
            return getItemStack() != null;
        }
        return false;
    }
}


