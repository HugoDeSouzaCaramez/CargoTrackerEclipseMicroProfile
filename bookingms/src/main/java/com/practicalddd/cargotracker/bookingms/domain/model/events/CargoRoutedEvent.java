package com.practicalddd.cargotracker.bookingms.domain.model.events;

import java.time.LocalDateTime;

public class CargoRoutedEvent implements DomainEvent {
    private final String bookingId;
    private final int legCount;
    private final LocalDateTime routingDate;
    private final String itinerarySummary; // Novo campo
    private final LocalDateTime occurredOn;

    public CargoRoutedEvent(String bookingId, int legCount, LocalDateTime routingDate) {
        this(bookingId, legCount, routingDate, "No summary");
    }
    
    public CargoRoutedEvent(String bookingId, int legCount, LocalDateTime routingDate, String itinerarySummary) {
        this.bookingId = bookingId;
        this.legCount = legCount;
        this.routingDate = routingDate;
        this.itinerarySummary = itinerarySummary;
        this.occurredOn = LocalDateTime.now();
    }

    public String getBookingId() { return bookingId; }
    public int getLegCount() { return legCount; }
    public LocalDateTime getRoutingDate() { return routingDate; }
    
    @Override
    public LocalDateTime occurredOn() { return occurredOn; }
    
    @Override
    public String eventType() { return "CargoRoutedEvent"; }
    
     public String getItinerarySummary() { return itinerarySummary; }
    
    @Override
    public String toString() {
        return String.format("CargoRoutedEvent{bookingId='%s', legCount=%d, summary='%s'}", 
            bookingId, legCount, itinerarySummary);
    }
}
