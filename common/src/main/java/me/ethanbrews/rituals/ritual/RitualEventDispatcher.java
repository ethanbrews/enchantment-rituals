package me.ethanbrews.rituals.ritual;

import java.util.HashSet;
import java.util.Set;

public class RitualEventDispatcher {
    private RitualTimeline timeline;
    private int tick;
    private RitualEvent currentEvent;
    private Set<RitualEventHandler> handlers;

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

    public void unsubscribe(RitualEventHandler handler) {
        handlers.remove(handler);
    }

    public void tick() throws RitualException {
        if (currentEvent == null) {
            currentEvent = timeline.nextEvent();
            if (currentEvent == null) {
                throw new RitualException("Ticking an expired ritual timeline");
            }
            var frozenHandlers = new HashSet<>(handlers);
            switch (currentEvent.getType()) {
                case START_CONSUME_ITEM -> {
                    frozenHandlers.forEach(h -> h.startItemConsumePhase(currentEvent.getPedestal()));
                }
                case END_CONSUME_ITEM -> {
                    frozenHandlers.forEach(h -> h.endItemConsumePhase(currentEvent.getPedestal()));
                }
                case FAIL -> {
                    frozenHandlers.forEach(h -> h.failure());
                }
                case SUCCEED -> {
                    frozenHandlers.forEach(h -> h.success());
                }
            }
        }
        tick++;
        if (currentEvent.getTime() > tick) {
            return;
        }
        tick = 0;
        currentEvent = null;
    }
}
