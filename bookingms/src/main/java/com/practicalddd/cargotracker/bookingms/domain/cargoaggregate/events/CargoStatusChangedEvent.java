package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events;

import com.practicalddd.cargotracker.bookingms.application.events.DomainEvent;
import java.time.LocalDateTime;

public class CargoStatusChangedEvent implements DomainEvent {
    private final String bookingId;
    private final String oldStatus;
    private final String newStatus;
    private final String reason;
    private final LocalDateTime occurredOn;

    public CargoStatusChangedEvent(String bookingId, String oldStatus, 
                                   String newStatus, String reason) {
        this.bookingId = bookingId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.occurredOn = LocalDateTime.now();
    }

    public String getBookingId() { return bookingId; }
    public String getOldStatus() { return oldStatus; }
    public String getNewStatus() { return newStatus; }
    public String getReason() { return reason; }
    
    @Override
    public LocalDateTime occurredOn() { return occurredOn; }
    
    @Override
    public String eventType() { return "CargoStatusChangedEvent"; }
}
