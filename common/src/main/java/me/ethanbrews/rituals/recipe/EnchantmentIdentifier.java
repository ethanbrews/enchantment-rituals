package me.ethanbrews.rituals.recipe;

import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.util.EnchantmentHelper;
import net.minecraft.ResourceLocationException;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public record EnchantmentIdentifier(
        @NotNull String enchantment,
        @NotNull Integer level
) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public Enchantment getEnchantment() {
        try {
            return EnchantmentHelper.getEnchantment(enchantment);
        } catch (ResourceLocationException e) {
            LOGGER.error("Invalid enchantment resource location: {}", enchantment);
            return null;
        }
    }

    public Integer getLevel() {
        return level;
    }
}
