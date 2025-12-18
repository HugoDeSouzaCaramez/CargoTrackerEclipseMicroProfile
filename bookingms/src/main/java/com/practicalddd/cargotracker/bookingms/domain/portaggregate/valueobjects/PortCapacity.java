package com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects;

import java.util.Objects;

public class PortCapacity {
    private final int currentUsage;
    private final int maxCapacity;

    public PortCapacity(int currentUsage, int maxCapacity) {
        validateCapacity(currentUsage, maxCapacity);
        this.currentUsage = currentUsage;
        this.maxCapacity = maxCapacity;
    }

    private void validateCapacity(int currentUsage, int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Max capacity must be positive");
        }
        if (currentUsage < 0) {
            throw new IllegalArgumentException("Current usage cannot be negative");
        }
        if (currentUsage > maxCapacity) {
            throw new IllegalArgumentException("Current usage cannot exceed max capacity");
        }
    }

    // Métodos de negócio
    public boolean canAccommodate(int additionalCargo) {
        return currentUsage + additionalCargo <= maxCapacity;
    }

    public PortCapacity addUsage(int cargoAmount) {
        return new PortCapacity(currentUsage + cargoAmount, maxCapacity);
    }

    public PortCapacity reduceUsage(int cargoAmount) {
        return new PortCapacity(Math.max(0, currentUsage - cargoAmount), maxCapacity);
    }

    public int getAvailableCapacity() {
        return maxCapacity - currentUsage;
    }

    public double getUsagePercentage() {
        return (double) currentUsage / maxCapacity * 100;
    }

    // Getters
    public int getCurrentUsage() { return currentUsage; }
    public int getMaxCapacity() { return maxCapacity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortCapacity)) return false;
        PortCapacity that = (PortCapacity) o;
        return currentUsage == that.currentUsage && maxCapacity == that.maxCapacity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentUsage, maxCapacity);
    }
}
