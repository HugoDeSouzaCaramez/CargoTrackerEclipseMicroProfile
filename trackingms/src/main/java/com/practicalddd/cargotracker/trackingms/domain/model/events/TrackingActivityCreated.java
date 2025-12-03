package com.practicalddd.cargotracker.trackingms.domain.model.events;

import java.time.LocalDateTime;

public class TrackingActivityCreated {
    
    private final String trackingNumber;
    private final String bookingId;
    private final LocalDateTime occurredOn;

    public TrackingActivityCreated(String trackingNumber, String bookingId) {
        this.trackingNumber = trackingNumber;
        this.bookingId = bookingId;
        this.occurredOn = LocalDateTime.now();
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getBookingId() {
        return bookingId;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
