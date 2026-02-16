package me.ethanbrews.rituals.compat.jei;

import me.ethanbrews.rituals.EnchantmentRituals;
import me.ethanbrews.rituals.recipe.EnchantmentRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class EnchantmentRitualRecipe implements IEnchantmentRitualRecipe {

    private final EnchantmentRecipe recipe;
    private final Item[][] parsedIngredients;

    public EnchantmentRitualRecipe(EnchantmentRecipe r) {
        this.recipe = r;
        this.parsedIngredients = r.getIngredients();
    }

    @Override
    public int ingredientsCount() {
        return recipe.getIngredients().length;
    }

    @Override
    public ItemStack getIngredient(int index) {
        return parsedIngredients[index][0].getDefaultInstance();
    }

    @Override
    public List<ItemStack> getIngredients() {
        return Arrays.stream(parsedIngredients).map(l -> l[0].getDefaultInstance()).toList();
    }
}
