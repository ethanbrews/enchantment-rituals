package me.ethanbrews.rituals.compat.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IEnchantmentRitualRecipe {
    int ingredientsCount();
    ItemStack getIngredient(int index);
    List<ItemStack> getIngredients();

}
