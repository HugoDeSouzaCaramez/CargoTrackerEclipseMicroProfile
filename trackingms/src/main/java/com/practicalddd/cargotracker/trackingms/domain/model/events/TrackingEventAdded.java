package com.practicalddd.cargotracker.trackingms.domain.model.events;

import java.util.Date;

public class TrackingEventAdded {
    
    private final String bookingId;
    private final String eventType;
    private final String location;
    private final Date eventTime;
    private final Date occurredOn;

    public TrackingEventAdded(String bookingId, String eventType, String location, Date eventTime) {
        this.bookingId = bookingId;
        this.eventType = eventType;
        this.location = location;
        this.eventTime = new Date(eventTime.getTime()); // Defensive copy
        this.occurredOn = new Date();
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

    public Date getEventTime() {
        return new Date(eventTime.getTime()); // Defensive copy
    }

    public Date getOccurredOn() {
        return new Date(occurredOn.getTime()); // Defensive copy
    }
}
