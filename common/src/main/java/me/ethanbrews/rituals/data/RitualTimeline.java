package me.ethanbrews.rituals.data;

import me.ethanbrews.rituals.RitualException;

import java.util.ArrayList;
import java.util.List;

public class RitualTimeline {
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
}
