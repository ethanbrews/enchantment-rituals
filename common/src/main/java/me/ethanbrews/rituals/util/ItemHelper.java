package me.ethanbrews.rituals.util;

import com.mojang.logging.LogUtils;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class ItemHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static @NotNull Collection<Item> getItemsMatchingIdentifier(@NotNull String itemId) throws ResourceLocationException {
        Set<Item> validItems = new LinkedHashSet<>(); // Use Set to avoid duplicates
        if (itemId.startsWith("#")) {
            // It's a tag
            ResourceLocation tagLocation = new ResourceLocation(itemId.substring(1));
            TagKey<Item> tag = TagKey.create(Registries.ITEM, tagLocation);

            // Get all items in the tag
            BuiltInRegistries.ITEM.getTag(tag).ifPresent(holders -> {
                holders.forEach(holder -> validItems.add(holder.value()));
            });

            if (validItems.isEmpty()) {
                LOGGER.error("Tag is empty or doesn't exist: {}", itemId);
            }

        } else {
            // It's a direct item
            ResourceLocation resourceLocation = new ResourceLocation(itemId);
            Item item = BuiltInRegistries.ITEM.get(resourceLocation);

            if (item != Items.AIR) {
                validItems.add(item);
            } else {
                LOGGER.error("Unknown item: {}", itemId);
            }
        }

        return validItems;
    }
}
