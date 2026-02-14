package me.ethanbrews.rituals.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeHelper {
    /**
     * Validates ingredients using a greedy matching approach.
     * More efficient for most cases, but uses backtracking for ambiguous cases.
     */
    public static boolean validateIngredients(List<ItemStack> ingredients, Item[][] recipeItems) {
        if (recipeItems == null || ingredients.size() != recipeItems.length) {
            return false;
        }

        // Build a list of possible slot matches for each ingredient
        List<Set<Integer>> possibleMatches = new ArrayList<>();

        for (ItemStack ingredient : ingredients) {
            Set<Integer> matches = new HashSet<>();

            for (int slotIndex = 0; slotIndex < recipeItems.length; slotIndex++) {
                for (Item validItem : recipeItems[slotIndex]) {
                    if (ingredient.is(validItem)) {
                        matches.add(slotIndex);
                        break;
                    }
                }
            }

            if (matches.isEmpty()) {
                return false; // This ingredient doesn't match any slot
            }

            possibleMatches.add(matches);
        }

        // Use backtracking to find a valid complete matching
        boolean[] usedSlots = new boolean[recipeItems.length];
        return findMatchingIngredient(possibleMatches, 0, usedSlots);
    }

    private static boolean findMatchingIngredient(List<Set<Integer>> possibleMatches,
                                                  int ingredientIndex, boolean[] usedSlots) {
        if (ingredientIndex == possibleMatches.size()) {
            return true;
        }

        Set<Integer> matches = possibleMatches.get(ingredientIndex);

        for (int slotIndex : matches) {
            if (!usedSlots[slotIndex]) {
                usedSlots[slotIndex] = true;

                if (findMatchingIngredient(possibleMatches, ingredientIndex + 1, usedSlots)) {
                    return true;
                }

                usedSlots[slotIndex] = false;
            }
        }

        return false;
    }
}
