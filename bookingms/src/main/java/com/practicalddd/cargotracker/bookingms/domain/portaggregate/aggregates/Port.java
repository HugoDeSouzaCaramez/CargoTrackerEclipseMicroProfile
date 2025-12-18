package com.practicalddd.cargotracker.bookingms.domain.portaggregate.aggregates;

import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortId;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortCapacity;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortLocation;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Agregado Port - Raiz de agregação para portos
 * Mantido simples para experimentos de cross-aggregation
 */
public class Port {

    private PortId portId;
    private PortLocation location;
    private PortCapacity capacity;
    private PortStatus status;
    private LocalDateTime lastUpdated;
    
    // Construtor privado
    private Port() {}

    public Port(PortId portId, PortLocation location, PortCapacity capacity) {
        validateConstruction(portId, location, capacity);
        
        this.portId = portId;
        this.location = location;
        this.capacity = capacity;
        this.status = PortStatus.OPERATIONAL;
        this.lastUpdated = LocalDateTime.now();
    }

    // Métodos de negócio
    public boolean canAccommodate(int cargoAmount) {
        return capacity.canAccommodate(cargoAmount);
    }

    public void updateCapacity(int newMaxCapacity) {
        validateCapacityUpdate(newMaxCapacity);
        this.capacity = new PortCapacity(capacity.getCurrentUsage(), newMaxCapacity);
        this.lastUpdated = LocalDateTime.now();
    }

    public void recordCargoArrival(int cargoAmount) {
        validateCargoArrival(cargoAmount);
        this.capacity = capacity.addUsage(cargoAmount);
        this.lastUpdated = LocalDateTime.now();
    }

    public void recordCargoDeparture(int cargoAmount) {
        this.capacity = capacity.reduceUsage(cargoAmount);
        this.lastUpdated = LocalDateTime.now();
    }

    public void markAsCongested() {
        if (capacity.getUsagePercentage() > 80) {
            this.status = PortStatus.CONGESTED;
        }
    }

    public void markAsOperational() {
        if (capacity.getUsagePercentage() <= 80) {
            this.status = PortStatus.OPERATIONAL;
        }
    }

    // Validações
    private void validateConstruction(PortId portId, PortLocation location, PortCapacity capacity) {
        if (portId == null) throw new IllegalArgumentException("Port ID cannot be null");
        if (location == null) throw new IllegalArgumentException("Location cannot be null");
        if (capacity == null) throw new IllegalArgumentException("Capacity cannot be null");
    }

    private void validateCapacityUpdate(int newMaxCapacity) {
        if (newMaxCapacity <= 0) {
            throw new IllegalArgumentException("Max capacity must be positive");
        }
        if (newMaxCapacity < capacity.getCurrentUsage()) {
            throw new IllegalArgumentException("New max capacity cannot be less than current usage");
        }
    }

    private void validateCargoArrival(int cargoAmount) {
        if (cargoAmount <= 0) {
            throw new IllegalArgumentException("Cargo amount must be positive");
        }
        if (!canAccommodate(cargoAmount)) {
            throw new IllegalStateException("Port cannot accommodate additional cargo");
        }
    }

    // Getters
    public PortId getPortId() { return portId; }
    public PortLocation getLocation() { return location; }
    public PortCapacity getCapacity() { return capacity; }
    public PortStatus getStatus() { return status; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public String getUnLocCode() { return location.getUnLocCode(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Port)) return false;
        Port port = (Port) o;
        return Objects.equals(portId, port.portId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(portId);
    }
}
