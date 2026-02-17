package me.ethanbrews.rituals.ritual;

import me.ethanbrews.rituals.block.EnchantPedestalBlockEntity;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RitualEventHandler {
    void startItemConsumePhase(@NotNull EnchantPedestalBlockEntity e, @NotNull Item expectItem);
    void endItemConsumePhase(@NotNull EnchantPedestalBlockEntity e);
    void consumeXp(int amount);
    void success();
    void failure(@Nullable EnchantPedestalBlockEntity e);
}
