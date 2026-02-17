package me.ethanbrews.rituals.recipe;

import me.ethanbrews.rituals.EnchantmentRituals;
import me.ethanbrews.rituals.util.RecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantmentRecipeRegistry {
    private static Map<ResourceLocation, EnchantmentRecipe> recipeMap;

    public static void loadRecipes(ResourceManager resourceManager) {
        recipeMap = RecipeHelper.loadRecipes(resourceManager)
                .entrySet()
                .stream()
                .filter(pair -> pair.getValue().isValidRecipe())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static List<EnchantmentRecipe> getEnchantmentRecipes() {
        return new ArrayList<>(recipeMap.values());
    }
}
