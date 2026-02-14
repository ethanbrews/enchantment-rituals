package me.ethanbrews.rituals.util;

import net.minecraft.world.entity.player.Player;

public class XpHelper {
    /**
     * Calculates the actual XP point cost to remove a number of levels from the player.
     * Takes into account the player's current level and progress.
     *
     * @param player The player
     * @param levels Number of levels to remove
     * @return The actual XP point cost, or -1 if player doesn't have enough levels
     */
    public static int getLevelCostInXP(Player player, int levels) {
        if (player.experienceLevel < levels) {
            return -1; // Not enough levels
        }

        int currentTotalXP = getTotalXP(player);
        int targetLevel = player.experienceLevel - levels;
        int targetTotalXP = getXPForLevel(targetLevel);

        return currentTotalXP - targetTotalXP;
    }

    // Helper: Get player's current total XP
    private static int getTotalXP(Player player) {
        return getXPForLevel(player.experienceLevel) +
                (int)(player.experienceProgress * player.getXpNeededForNextLevel());
    }

    // Helper: Get cumulative XP for a level
    private static int getXPForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int)(2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int)(4.5 * level * level - 162.5 * level + 2220);
        }
    }
}
