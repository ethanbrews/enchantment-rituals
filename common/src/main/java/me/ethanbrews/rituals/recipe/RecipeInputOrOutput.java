package me.ethanbrews.rituals.recipe;

import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.util.ItemHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;

public record RecipeInputOrOutput(
        @Nullable EnchantmentIdentifier enchantment,
        @Nullable String item
) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public boolean isValidOutput() {
        if ((enchantment == null && item == null) || (enchantment != null && item != null)) {
            LOGGER.error("Exactly one of enchantment or item must be specified");
            return false;
        }
        return true;
    }

    public boolean isValidInput() {
        if ((enchantment == null && item == null)) {
            LOGGER.error("At least one of enchantment or item must be specified");
            return false;
        }
        return true;
    }

    public boolean isEnchantment() {
        return enchantment != null;
    }

    public boolean isItem() {
        return item != null;
    }

    public EnchantmentIdentifier getEnchantmentIdentifier() {
        return enchantment;
    }

    public @Nullable ItemStack getItemStack() {
        var item = Objects.requireNonNull(item());
        var candidates = ItemHelper.getItemsMatchingIdentifier(item);
        if (candidates.isEmpty()) {
            LOGGER.error("Invalid item resource location: {}", item);
            return null;
        } else {
            return candidates.stream().findFirst().get().getDefaultInstance();
        }
    }
}
