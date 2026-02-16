package me.ethanbrews.rituals.recipe;

import org.jetbrains.annotations.NotNull;

public record IngredientOption(
        @NotNull String[] items,
        @NotNull Integer priority  // Lower number = higher priority (0 = first choice)
) {}
