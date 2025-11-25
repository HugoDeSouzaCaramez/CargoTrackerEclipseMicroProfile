package com.practicalddd.cargotracker.bookingms.domain.model.events;

import java.time.LocalDateTime;

public class CargoRoutedEvent implements DomainEvent {
    private final String bookingId;
    private final LocalDateTime occurredOn;

    public CargoRoutedEvent(String bookingId) {
        this.bookingId = bookingId;
        this.occurredOn = LocalDateTime.now();
    }

    public String getBookingId() { return bookingId; }
    
    @Override
    public LocalDateTime occurredOn() { return occurredOn; }
    
    @Override
    public String eventType() { return "CargoRoutedEvent"; }
}