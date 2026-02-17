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
        int targetTotalXP = levelsToXp(targetLevel);

        return currentTotalXP - targetTotalXP;
    }

    /**
     * Get XP needed to go from level N to level N+1
     */
    public static int getXPNeededForLevel(int level) {
        if (level >= 30) {
            return 9 * level - 158;
        } else if (level >= 15) {
            return 5 * level - 38;
        } else {
            return 2 * level + 7;
        }
    }

    /**
     * Calculate total XP needed to reach a specific level from 0
     */
    public static int levelsToXp(int level) {
        if (level >= 30) {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        } else if (level >= 15) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return level * level + 6 * level;
        }
    }

    // Helper: Get player's current total XP
    public static int getTotalXP(Player player) {
        return levelsToXp(player.experienceLevel) +
                (int)(player.experienceProgress * player.getXpNeededForNextLevel());
    }
}
