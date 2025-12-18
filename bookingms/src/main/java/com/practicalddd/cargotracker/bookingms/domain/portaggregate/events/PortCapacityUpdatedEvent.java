package com.practicalddd.cargotracker.bookingms.domain.portaggregate.events;

import com.practicalddd.cargotracker.bookingms.application.events.DomainEvent;
import java.time.LocalDateTime;

public class PortCapacityUpdatedEvent implements DomainEvent {
    private final String portUnLocCode;
    private final int oldCapacity;
    private final int newCapacity;
    private final LocalDateTime occurredOn;

    public PortCapacityUpdatedEvent(String portUnLocCode, int oldCapacity, int newCapacity) {
        this.portUnLocCode = portUnLocCode;
        this.oldCapacity = oldCapacity;
        this.newCapacity = newCapacity;
        this.occurredOn = LocalDateTime.now();
    }

    // Getters
    public String getPortUnLocCode() { return portUnLocCode; }
    public int getOldCapacity() { return oldCapacity; }
    public int getNewCapacity() { return newCapacity; }
    
    // Implementação dos métodos da interface DomainEvent
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "PortCapacityUpdatedEvent";
    }
}
