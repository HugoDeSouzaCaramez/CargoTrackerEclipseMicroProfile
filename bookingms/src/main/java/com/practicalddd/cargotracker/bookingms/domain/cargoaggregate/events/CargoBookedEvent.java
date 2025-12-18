package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events;

import com.practicalddd.cargotracker.bookingms.application.events.DomainEvent;
import java.time.LocalDateTime;

public class CargoBookedEvent implements DomainEvent {
    private final String bookingId;
    private final int bookingAmount;
    private final String originLocation;
    private final String destLocation;
    private final LocalDateTime destArrivalDeadline;
    private final LocalDateTime occurredOn;

    public CargoBookedEvent(String bookingId, int bookingAmount, String originLocation, 
                           String destLocation, LocalDateTime destArrivalDeadline) {
        this.bookingId = bookingId;
        this.bookingAmount = bookingAmount;
        this.originLocation = originLocation;
        this.destLocation = destLocation;
        this.destArrivalDeadline = destArrivalDeadline;
        this.occurredOn = LocalDateTime.now();
    }

    public String getBookingId() { return bookingId; }
    public int getBookingAmount() { return bookingAmount; }
    public String getOriginLocation() { return originLocation; }
    public String getDestLocation() { return destLocation; }
    public LocalDateTime getDestArrivalDeadline() { return destArrivalDeadline; }
    
    @Override
    public LocalDateTime occurredOn() { return occurredOn; }
    
    @Override
    public String eventType() { return "CargoBookedEvent"; }
}
