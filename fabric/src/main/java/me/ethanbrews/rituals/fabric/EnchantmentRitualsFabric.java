package me.ethanbrews.rituals.fabric;

import me.ethanbrews.rituals.recipe.EnchantmentRecipeRegistry;
import net.fabricmc.api.ModInitializer;

import me.ethanbrews.rituals.EnchantmentRituals;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class EnchantmentRitualsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        EnchantmentRituals.init();

        // Register /reload listener for ritual recipes
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return new ResourceLocation(EnchantmentRituals.MOD_ID, "rituals");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        EnchantmentRecipeRegistry.loadRecipes(resourceManager);
                    }
                }
        );
    }
}
