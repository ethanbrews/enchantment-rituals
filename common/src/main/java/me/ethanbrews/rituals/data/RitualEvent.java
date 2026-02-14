package me.ethanbrews.rituals.data;

import me.ethanbrews.rituals.RitualException;
import me.ethanbrews.rituals.block.EnchantPedestalBlockEntity;

public class RitualEvent {
    public enum RitualEventType {
        START_CONSUME_ITEM,
        END_CONSUME_ITEM,
        FAIL,
        SUCCEED
    }

    private final int time;
    private final RitualEventType type;
    private final EnchantPedestalBlockEntity pedestal;

    private RitualEvent(int time, RitualEventType type, EnchantPedestalBlockEntity pedestal) {
        this.time = time;
        this.type = type;
        this.pedestal = pedestal;
    }

    public RitualEventType getType() {
        return this.type;
    }

    public EnchantPedestalBlockEntity getPedestal() {
        return pedestal;
    }

    public int getTime() {
        return time;
    }

    public static RitualEvent succeed(int time) {
        return new RitualEvent(time, RitualEventType.SUCCEED, null);
    }

    public static RitualEvent fail(int time) {
        return new RitualEvent(time, RitualEventType.FAIL, null);
    }

    public static RitualEvent startConsume(int time, EnchantPedestalBlockEntity pedestal) {
        return new RitualEvent(time, RitualEventType.START_CONSUME_ITEM, pedestal);
    }

    public static RitualEvent endConsume(int time, EnchantPedestalBlockEntity pedestal) {
        return new RitualEvent(time, RitualEventType.END_CONSUME_ITEM, pedestal);
    }
}
