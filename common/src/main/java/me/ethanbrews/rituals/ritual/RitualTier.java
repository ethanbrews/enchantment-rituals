package me.ethanbrews.rituals.ritual;

public enum RitualTier {
    TIER1,
    TIER2,
    TIER3;

    public static RitualTier parseTier(int tier) {
        return switch (tier) {
            case 1 -> TIER1;
            case 2 -> TIER2;
            case 3 -> TIER3;
            default -> null;
        };
    }
}
