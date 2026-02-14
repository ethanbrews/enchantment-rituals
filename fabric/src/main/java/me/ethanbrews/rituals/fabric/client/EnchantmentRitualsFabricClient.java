package me.ethanbrews.rituals.fabric.client;

import me.ethanbrews.rituals.EnchantmentRituals;
import net.fabricmc.api.ClientModInitializer;

public final class EnchantmentRitualsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EnchantmentRituals.initClient();
    }
}
