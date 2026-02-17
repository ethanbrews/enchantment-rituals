package me.ethanbrews.rituals.compat.jei;

import me.ethanbrews.rituals.item.EnchantmentRitualItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.ethanbrews.rituals.util.MathHelper.roundUpToMultiple;

public class RitualCategory implements IRecipeCategory<IEnchantmentRitualRecipe> {
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;
    private static final int columns = 4;

    public RitualCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(EnchantmentRitualItems.ENCHANTMENT_PEDESTAL_BLOCK_ITEM.get())
        );
        this.slot = helper.getSlotDrawable();
        this.arrow = helper.getRecipeArrow();
    }

    @Override
    public @NotNull RecipeType<IEnchantmentRitualRecipe> getRecipeType() {
        return EnchantmentRitualsPlugin.RITUAL_RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.category.ritual");
    }

    @Override
    public void draw(
            @NotNull IEnchantmentRitualRecipe recipe,
            @NotNull IRecipeSlotsView recipeSlotsView,
            @NotNull GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);
        for (int i = 0; i < roundUpToMultiple(recipe.ingredientsCount(), columns); i++) {
            int col = i % columns;
            int row = i / columns;
            slot.draw(graphics, 69 + (col * 19), 2 + (row * 19));
        }
        arrow.draw(graphics, 36, 12);
        slot.draw(graphics, 9, 11);
    }

    @Override
    public int getWidth() {
        return 137;
    }

    @Override
    public int getHeight() {
        return 40;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            @NotNull IRecipeLayoutBuilder builder,
            @NotNull IEnchantmentRitualRecipe recipe,
            @NotNull IFocusGroup focuses
    ) {
        for (int i = 0; i < recipe.ingredientsCount(); i++) {
            int col = i % columns;
            int row = i / columns;
            builder.addSlot(RecipeIngredientRole.INPUT, 70 + (col * 19), 3 + (row * 19))
                    .addItemStack(recipe.getIngredient(i));
        }
    }
}
