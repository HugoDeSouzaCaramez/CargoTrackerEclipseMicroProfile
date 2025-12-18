package com.practicalddd.cargotracker.bookingms.domain.portaggregate.events;

import com.practicalddd.cargotracker.bookingms.application.events.DomainEvent;
import java.time.LocalDateTime;

public class PortCargoMovementRecordedEvent implements DomainEvent {
    private final String portUnLocCode;
    private final int cargoAmount;
    private final String movementType;
    private final int currentUsage;
    private final int maxCapacity;
    private final LocalDateTime occurredOn;

    public PortCargoMovementRecordedEvent(String portUnLocCode, int cargoAmount, 
                                        String movementType, int currentUsage, 
                                        int maxCapacity) {
        this.portUnLocCode = portUnLocCode;
        this.cargoAmount = cargoAmount;
        this.movementType = movementType;
        this.currentUsage = currentUsage;
        this.maxCapacity = maxCapacity;
        this.occurredOn = LocalDateTime.now();
    }

    // Getters
    public String getPortUnLocCode() { return portUnLocCode; }
    public int getCargoAmount() { return cargoAmount; }
    public String getMovementType() { return movementType; }
    public int getCurrentUsage() { return currentUsage; }
    public int getMaxCapacity() { return maxCapacity; }
    
    // Implementação dos métodos da interface DomainEvent
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "PortCargoMovementRecordedEvent";
    }
}
