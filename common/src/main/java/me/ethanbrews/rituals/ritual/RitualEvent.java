package me.ethanbrews.rituals.ritual;

import me.ethanbrews.rituals.block.EnchantPedestalBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

record ConsumeEventData(
    EnchantPedestalBlockEntity pedestal,
    Item ingredient
) {}

public class RitualEvent {
    public enum RitualEventType {
        START_CONSUME_ITEM,
        END_CONSUME_ITEM,
        CONSUME_XP,
        FAIL,
        SUCCEED
    }

    private final int time;
    private final RitualEventType type;
    private final Object data;

    private RitualEvent(int time, RitualEventType type, Object obj) {
        this.time = time;
        this.type = type;
        this.data = obj;
    }

    public RitualEventType getType() {
        return this.type;
    }

    public Object getData() {
        return data;
    }

    public int getTime() {
        return time;
    }

    public static RitualEvent succeed(int time) {
        return new RitualEvent(time, RitualEventType.SUCCEED, null);
    }

    public static RitualEvent fail(int time, @Nullable EnchantPedestalBlockEntity failureBlock) {
        return new RitualEvent(time, RitualEventType.FAIL, failureBlock);
    }

    public static RitualEvent startConsume(int time, EnchantPedestalBlockEntity pedestal, Item expectItem) {
        return new RitualEvent(time, RitualEventType.START_CONSUME_ITEM, new ConsumeEventData(pedestal, expectItem));
    }

    public static RitualEvent endConsume(int time, EnchantPedestalBlockEntity pedestal) {
        return new RitualEvent(time, RitualEventType.END_CONSUME_ITEM, pedestal);
    }

    public static RitualEvent consumeXp(int time, int xpAmount) {
        return new RitualEvent(time, RitualEventType.CONSUME_XP, xpAmount);
    }
}
