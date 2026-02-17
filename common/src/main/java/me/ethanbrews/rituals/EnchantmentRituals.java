package me.ethanbrews.rituals;

import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.block.EnchantmentRitualBlocks;
import me.ethanbrews.rituals.item.EnchantmentRitualItems;
import me.ethanbrews.rituals.particle.EnchantmentRitualParticles;
import me.ethanbrews.rituals.recipe.*;
import me.ethanbrews.rituals.util.RecipeHelper;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.util.List;

public final class EnchantmentRituals {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "enchantmentrituals";


    public static void init() {
        LOGGER.info("Hello from EnchantmentRituals.");
        LOGGER.debug("Debug message from EnchantmentRituals.");
        EnchantmentRitualBlocks.register();
        EnchantmentRitualItems.register();
        EnchantmentRitualParticles.register();
    }

    public static void initClient() {
        EnchantmentRitualBlocks.registerClient();
        EnchantmentRitualParticles.registerClient();
    }
}
