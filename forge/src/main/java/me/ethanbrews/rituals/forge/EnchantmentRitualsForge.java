package me.ethanbrews.rituals.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import me.ethanbrews.rituals.EnchantmentRituals;

@Mod(EnchantmentRituals.MOD_ID)
public final class EnchantmentRitualsForge {
    public EnchantmentRitualsForge(FMLJavaModLoadingContext ctx) {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(EnchantmentRituals.MOD_ID, ctx.getModEventBus());

        // Run our common setup.
        EnchantmentRituals.init();
    }
}
