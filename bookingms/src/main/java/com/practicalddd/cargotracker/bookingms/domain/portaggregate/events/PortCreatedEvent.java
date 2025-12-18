package com.practicalddd.cargotracker.bookingms.domain.portaggregate.events;

import com.practicalddd.cargotracker.bookingms.application.events.DomainEvent;
import java.time.LocalDateTime;

public class PortCreatedEvent implements DomainEvent {
    private final String portUnLocCode;
    private final String portName;
    private final String country;
    private final int initialCapacity;
    private final LocalDateTime occurredOn;

    public PortCreatedEvent(String portUnLocCode, String portName, String country, 
                          int initialCapacity) {
        this.portUnLocCode = portUnLocCode;
        this.portName = portName;
        this.country = country;
        this.initialCapacity = initialCapacity;
        this.occurredOn = LocalDateTime.now();
    }

    // Getters
    public String getPortUnLocCode() { return portUnLocCode; }
    public String getPortName() { return portName; }
    public String getCountry() { return country; }
    public int getInitialCapacity() { return initialCapacity; }
    
    // Implementação dos métodos da interface DomainEvent
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "PortCreatedEvent";
    }
}
