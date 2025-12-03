package com.practicalddd.cargotracker.trackingms.domain.model.valueobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value Object que representa a coleção de eventos de tracking.
 * Imutável - operações retornam novas instâncias.
 */
public class TrackingActivityEvents {
    
    public static final TrackingActivityEvents EMPTY = new TrackingActivityEvents();
    
    private final List<TrackingEvent> events;

    private TrackingActivityEvents() {
        this.events = Collections.emptyList();
    }

    public TrackingActivityEvents(List<TrackingEvent> events) {
        // Cópia defensiva
        this.events = events != null 
            ? Collections.unmodifiableList(new ArrayList<>(events))
            : Collections.emptyList();
    }

    public TrackingActivityEvents addEvent(TrackingEvent event) {
        if (event == null) return this;
        
        List<TrackingEvent> newEvents = new ArrayList<>(this.events);
        newEvents.add(event);
        return new TrackingActivityEvents(newEvents);
    }

    public List<TrackingEvent> getEvents() {
        return events; // Já é imutável
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public int count() {
        return events.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackingActivityEvents that = (TrackingActivityEvents) o;
        return Objects.equals(events, that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events);
    }
}