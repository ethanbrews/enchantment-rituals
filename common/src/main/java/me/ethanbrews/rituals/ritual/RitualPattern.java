package me.ethanbrews.rituals.ritual;


import me.ethanbrews.rituals.block.EnchantPedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public enum RitualPattern {
    PLUS(4) {
        @Override
        public List<EnchantPedestalBlockEntity> findPedestals(Level level, BlockPos center) {
            List<EnchantPedestalBlockEntity> pedestals = new ArrayList<>();

            // Distance from center to spoke pedestals
            int distance = 3; // 2 blocks between + 1 for the pedestal itself

            // Check all 4 cardinal directions
            BlockPos[] positions = {
                    center.north(distance),  // -Z
                    center.south(distance),  // +Z
                    center.east(distance),   // +X
                    center.west(distance)    // -X
            };

            for (BlockPos pos : positions) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof EnchantPedestalBlockEntity pedestal) {
                    pedestals.add(pedestal);
                }
            }

            return pedestals;
        }
    };

    private final int requiredPedestals;

    RitualPattern(int requiredPedestals) {
        this.requiredPedestals = requiredPedestals;
    }

    public int getRequiredPedestals() {
        return requiredPedestals;
    }

    /**
     * Finds pedestals matching this pattern around the center position.
     * @param level The level to search in
     * @param center The center position (controller pedestal)
     * @return List of found pedestals (may be incomplete if pattern doesn't match)
     */
    public abstract List<EnchantPedestalBlockEntity> findPedestals(Level level, BlockPos center);
}
