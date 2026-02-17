package me.ethanbrews.rituals.compat.jei;

import me.ethanbrews.rituals.EnchantmentRituals;
import me.ethanbrews.rituals.block.EnchantmentRitualBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EnchantmentRitualsPlugin implements IModPlugin {
    public final static RecipeType<IEnchantmentRitualRecipe> RITUAL_RECIPE_TYPE = RecipeType.create(
            EnchantmentRituals.MOD_ID,
            "processor",
            IEnchantmentRitualRecipe.class);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(EnchantmentRituals.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new RitualCategory(
                        registration.getJeiHelpers().getGuiHelper()
                )
        );

    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(
                new ItemStack(EnchantmentRitualBlocks.ENCHANTMENT_PEDESTAL_BLOCK.get()),
                RITUAL_RECIPE_TYPE
        );
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        registration.addRecipes(RITUAL_RECIPE_TYPE, RitualRecipeProvider.getAll());
    }
}
