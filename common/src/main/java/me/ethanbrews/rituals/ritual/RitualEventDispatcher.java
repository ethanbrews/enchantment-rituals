package me.ethanbrews.rituals.ritual;

import me.ethanbrews.rituals.block.EnchantPedestalBlockEntity;

import java.util.HashSet;
import java.util.Set;

public class RitualEventDispatcher {
    private final RitualTimeline timeline;
    private int tick;
    private RitualEvent currentEvent;
    private final Set<RitualEventHandler> handlers;

    public RitualEventDispatcher(RitualTimeline timeline) {
        this.timeline = timeline;
        handlers = new HashSet<>();
        this.tick = 0;
    }

    public void subscribe(RitualEventHandler handler) {
        if (!handlers.add(handler)) {
            throw new RuntimeException("Handler could not subscribe.");
        }
    }

    public void injectEventNow(RitualEvent event) {
        handleEvent(event);
    }

    public void unsubscribe(RitualEventHandler handler) {
        handlers.remove(handler);
    }

    public void tick() throws RitualException {
        if (currentEvent == null) {
            currentEvent = timeline.nextEvent();
            if (currentEvent == null) {
                throw new RitualException("Ticking an expired ritual timeline");
            }
            handleEvent(currentEvent);
        }
        tick++;
        if (currentEvent.getTime() > tick) {
            return;
        }
        currentEvent = null;
    }

    private void handleEvent(RitualEvent event) {
        var frozenHandlers = new HashSet<>(handlers);
        switch (event.getType()) {
            case START_CONSUME_ITEM -> {
                var data = (ConsumeEventData) event.getData();
                frozenHandlers.forEach(h -> h.startItemConsumePhase(data.pedestal(), data.ingredient()));
            }
            case END_CONSUME_ITEM -> frozenHandlers.forEach(h -> h.endItemConsumePhase((EnchantPedestalBlockEntity) event.getData()));
            case CONSUME_XP -> frozenHandlers.forEach(h -> h.consumeXp((int) event.getData()));
            case FAIL -> frozenHandlers.forEach(h -> h.failure((EnchantPedestalBlockEntity) event.getData()));
            case SUCCEED -> frozenHandlers.forEach(RitualEventHandler::success);
        }
    }
}
