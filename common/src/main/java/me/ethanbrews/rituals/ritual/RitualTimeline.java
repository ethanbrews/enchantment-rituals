package me.ethanbrews.rituals.ritual;

import com.mojang.logging.LogUtils;
import me.ethanbrews.rituals.block.EnchantPedestalBlockEntity;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class RitualTimeline {
    private static final Logger LOGGER = LogUtils.getLogger();
    List<RitualEvent> events;
    Integer index = null;

    public RitualTimeline() {
        this(new ArrayList<>());
    }

    public RitualTimeline(List<RitualEvent> events) {
        this.events = events;
    }

    public void addEvent(RitualEvent event) {
        if (this.isStarted()) {
            throw new IllegalStateException("Cannot add an event to a started ritual timeline");
        }
        for (var i = 0; i < events.size(); i++) {
            if (events.get(i).getTime() > event.getTime()) {
                events.add(i, event);
                return;
            }
        }
        events.add(event);
    }

    public boolean isStarted() {
        return index != null;
    }

    public RitualEvent nextEvent() {
        if (index == null) {
            index = 0;
        } else {
            index++;
        }

        try {
            return events.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void emitTimelineDebugLogs() {
        LOGGER.info("=== Emitting timeline debug logs ===");
        for (var event : this.events) {
            LOGGER.info("{} ticks -> {} @ {}", event.getTime(), event.getType(), event.getData());
        }
        LOGGER.info("=== End timeline debug logs ===");
    }
}
