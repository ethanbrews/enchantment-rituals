package me.ethanbrews.rituals.ritual;

import me.ethanbrews.rituals.block.EnchantPedestalBlockEntity;

public interface RitualEventHandler {
    void startItemConsumePhase(EnchantPedestalBlockEntity e);
    void endItemConsumePhase(EnchantPedestalBlockEntity e);
    void success();
    void failure();
}
