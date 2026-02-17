package me.ethanbrews.rituals.compat.jei;

import me.ethanbrews.rituals.recipe.EnchantmentRecipeRegistry;

import java.util.List;

public class RitualRecipeProvider {

    public static List<IEnchantmentRitualRecipe> getAll() {
        return EnchantmentRecipeRegistry.getEnchantmentRecipes().stream()
                .map(r -> (IEnchantmentRitualRecipe) new EnchantmentRitualRecipe(r))
                .toList();
    }
}
