package com.practicalddd.cargotracker.trackingms.domain.model.valueobjects;

import java.time.LocalDateTime;
import java.util.Objects;

public class TrackingEventType {
    
    private final String eventType;
    private final LocalDateTime eventTime;

    public TrackingEventType(String eventType, LocalDateTime eventTime) {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
        if (eventTime == null) {
            throw new IllegalArgumentException("Event time cannot be null");
        }
        this.eventType = eventType;
        this.eventTime = eventTime; // LocalDateTime já é imutável, não precisa de cópia defensiva
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDateTime getEventTime() {
        return eventTime; // LocalDateTime é imutável, retorno seguro
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
    
    @Override
    public String toString() {
        return "TrackingEventType{" +
                "eventType='" + eventType + '\'' +
                ", eventTime=" + eventTime +
                '}';
    }
}
