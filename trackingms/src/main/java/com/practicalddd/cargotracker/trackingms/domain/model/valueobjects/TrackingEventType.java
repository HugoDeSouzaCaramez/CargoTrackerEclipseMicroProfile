package com.practicalddd.cargotracker.trackingms.domain.model.valueobjects;

import java.util.Date;
import java.util.Objects;

public class TrackingEventType {
    
    private final String eventType;
    private final Date eventTime;

    public TrackingEventType(String eventType, Date eventTime) {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
        if (eventTime == null) {
            throw new IllegalArgumentException("Event time cannot be null");
        }
        this.eventType = eventType;
        this.eventTime = new Date(eventTime.getTime()); // Defensive copy
    }

    public String getEventType() {
        return eventType;
    }

    public Date getEventTime() {
        return new Date(eventTime.getTime()); // Defensive copy
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackingEventType that = (TrackingEventType) o;
        return eventType.equals(that.eventType) &&
               eventTime.equals(that.eventTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, eventTime);
    }
}
