package me.ethanbrews.rituals.util;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

public class EnchantmentHelper {
    /**
     * Parses the enchantment string to an Enchantment.
     * Supports direct enchantments ("minecraft:sharpness").
     * Tags are not supported for single enchantment results.
     * Result is cached after first parse.
     *
     * @return The Enchantment, or null if invalid
     */
    public static Enchantment getEnchantment(@Nullable String enchantment) throws ResourceLocationException {
        if (enchantment == null || enchantment.isEmpty()) {
            return null;
        }
        ResourceLocation resourceLocation = new ResourceLocation(enchantment);
        var ench = BuiltInRegistries.ENCHANTMENT.get(resourceLocation);

        if (ench == null) {
            throw new ResourceLocationException("Unknown enchantment: " + enchantment);
        }

        return ench;
    }

    public static int getLevel(Enchantment enchantment, ItemStack itemStack) {
        int currentLevel = 0;
        var appliedEnchantments = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(itemStack);
        if (appliedEnchantments.containsKey(enchantment)) {
            currentLevel = appliedEnchantments.get(enchantment);
        }
        return currentLevel;
    }
}
