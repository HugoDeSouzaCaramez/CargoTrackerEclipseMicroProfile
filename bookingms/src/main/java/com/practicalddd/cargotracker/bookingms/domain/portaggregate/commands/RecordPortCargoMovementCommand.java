package com.practicalddd.cargotracker.bookingms.domain.portaggregate.commands;

public class RecordPortCargoMovementCommand {
    private final String portUnLocCode;
    private final int cargoAmount;
    private final MovementType movementType;
    
    public enum MovementType {
        ARRIVAL,
        DEPARTURE
    }

    public RecordPortCargoMovementCommand(String portUnLocCode, int cargoAmount, MovementType movementType) {
        this.portUnLocCode = portUnLocCode;
        this.cargoAmount = cargoAmount;
        this.movementType = movementType;
    }

    // Getters
    public String getPortUnLocCode() { return portUnLocCode; }
    public int getCargoAmount() { return cargoAmount; }
    public MovementType getMovementType() { return movementType; }
}
