package com.practicalddd.cargotracker.trackingms.domain.model.events;

import java.time.LocalDateTime;

public class TrackingEventAdded {
    
    private final String bookingId;
    private final String eventType;
    private final String location;
    private final LocalDateTime eventTime;
    private final LocalDateTime occurredOn;

    public TrackingEventAdded(String bookingId, String eventType, String location, LocalDateTime eventTime) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be null or empty");
        }
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }
        if (eventTime == null) {
            throw new IllegalArgumentException("Event time cannot be null");
        }
        
        this.bookingId = bookingId;
        this.eventType = eventType;
        this.location = location;
        this.eventTime = eventTime; // LocalDateTime já é imutável
        this.occurredOn = LocalDateTime.now(); // Momento atual
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getEventTime() {
        return eventTime; // LocalDateTime é imutável, retorno seguro
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn; // LocalDateTime é imutável, retorno seguro
    }
    
    @Override
    public String toString() {
        return "TrackingEventAdded{" +
                "bookingId='" + bookingId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", location='" + location + '\'' +
                ", eventTime=" + eventTime +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
