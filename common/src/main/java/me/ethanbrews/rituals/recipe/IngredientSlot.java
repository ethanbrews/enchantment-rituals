package me.ethanbrews.rituals.recipe;

import me.ethanbrews.rituals.util.ItemHelper;
import net.minecraft.ResourceLocationException;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public record IngredientSlot(
        @NotNull IngredientOption[] options
) {
    /**
     * Returns all valid items for this slot as an array of alternatives.
     * Items are ordered by priority (lower priority number first), with all items
     * within the same priority tier grouped together.
     *
     * Invalid item identifiers are silently skipped. Only throws if no valid items
     * are found for this slot at all.
     *
     * @return Array of Item alternatives that can fill this slot
     * @throws ResourceLocationException if no valid items are found for this slot
     */
    public Item[] getIngredients() throws ResourceLocationException {
        // Sort options by priority
        IngredientOption[] sortedOptions = Arrays.stream(options)
                .sorted(Comparator.comparingInt(IngredientOption::priority))
                .toArray(IngredientOption[]::new);

        List<Item> allItems = new ArrayList<>();

        // For each priority tier, resolve all items
        for (IngredientOption option : sortedOptions) {
            for (String itemId : option.items()) {
                try {
                    Collection<Item> matchingItems = ItemHelper.getItemsMatchingIdentifier(itemId);
                    allItems.addAll(matchingItems);
                } catch (ResourceLocationException e) {
                    // Silently skip invalid items - this allows graceful fallback
                    // to lower priority options or mod compatibility
                }
            }
        }

        // Only throw if we found nothing at all
        if (allItems.isEmpty()) {
            String allAttemptedIds = Arrays.stream(sortedOptions)
                    .flatMap(opt -> Arrays.stream(opt.items()))
                    .collect(Collectors.joining(", "));
            throw new ResourceLocationException(
                    "No valid items found for ingredient slot. Attempted: " + allAttemptedIds
            );
        }

        return allItems.toArray(new Item[0]);
    }
}
