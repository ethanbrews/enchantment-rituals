package me.ethanbrews.rituals.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.recipe.EnchantmentRecipe;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

import static me.ethanbrews.rituals.EnchantmentRituals.MOD_ID;

public class RecipeHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private static final String RECIPE_PATH = "rituals";

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

    /**
     * Load all enchantment recipes from resources
     *
     * @param resourceManager The resource manager
     * @return Map of recipe ID to EnchantmentRecipe
     */
    public static Map<ResourceLocation, EnchantmentRecipe> loadRecipes(ResourceManager resourceManager) {
        Map<ResourceLocation, EnchantmentRecipe> recipes = new HashMap<>();

        // Get all resources matching the pattern
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                RECIPE_PATH,
                location -> location.getNamespace().equals(MOD_ID)
                        && location.getPath().endsWith(".json")
        );

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation fileLocation = entry.getKey();
            Resource resource = entry.getValue();

            // Extract recipe ID from path
            // e.g., "enchantmentrituals:rituals/sharpness_1.json" -> "enchantmentrituals:sharpness_1"
            String path = fileLocation.getPath();
            String recipeName = path
                    .replace(RECIPE_PATH + "/", "")
                    .replace(".json", "");
            ResourceLocation recipeId = new ResourceLocation(MOD_ID, recipeName);

            try (Reader reader = new InputStreamReader(resource.open())) {
                EnchantmentRecipe recipe = GSON.fromJson(reader, EnchantmentRecipe.class);

                if (recipe == null) {
                    LOGGER.error("Failed to parse recipe: {} (null result)", recipeId);
                    continue;
                }

                // Validate recipe
                try {
                    recipe.getIngredients(); // This will throw if any slots are invalid
                    recipes.put(recipeId, recipe);
                    LOGGER.info("Loaded recipe: {}", recipeId);
                } catch (ResourceLocationException e) {
                    LOGGER.error("Invalid recipe {}: {}", recipeId, e.getMessage());
                }

            } catch (IOException e) {
                LOGGER.error("Failed to read recipe file: {}", fileLocation, e);
            } catch (JsonParseException e) {
                LOGGER.error("Failed to parse recipe JSON: {}", fileLocation, e);
            }
        }

        LOGGER.info("Loaded {} enchantment recipes", recipes.size());
        return recipes;
    }
}
