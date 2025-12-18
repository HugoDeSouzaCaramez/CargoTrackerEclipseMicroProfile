package com.practicalddd.cargotracker.bookingms.domain.portaggregate.commands;

public class UpdatePortCapacityCommand {
    private final String portUnLocCode;
    private final int newMaxCapacity;

    public UpdatePortCapacityCommand(String portUnLocCode, int newMaxCapacity) {
        this.portUnLocCode = portUnLocCode;
        this.newMaxCapacity = newMaxCapacity;
    }

    public String getPortUnLocCode() { return portUnLocCode; }
    public int getNewMaxCapacity() { return newMaxCapacity; }
}
