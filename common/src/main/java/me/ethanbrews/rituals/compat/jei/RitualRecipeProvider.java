package me.ethanbrews.rituals.compat.jei;

import me.ethanbrews.rituals.EnchantmentRituals;

import java.util.List;

public class RitualRecipeProvider {

    public static List<IEnchantmentRitualRecipe> getAll() {
        return EnchantmentRituals.getEnchantmentRecipes().stream()
                .map(r -> (IEnchantmentRitualRecipe) new EnchantmentRitualRecipe(r))
                .toList();
    }
}
