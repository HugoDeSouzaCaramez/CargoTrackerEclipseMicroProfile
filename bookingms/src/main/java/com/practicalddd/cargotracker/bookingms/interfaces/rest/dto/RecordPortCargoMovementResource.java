package com.practicalddd.cargotracker.bookingms.interfaces.rest.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RecordPortCargoMovementResource {
    
    @NotBlank(message = "Port UN/LOCODE is required")
    @Size(min = 5, max = 5, message = "UN/LOCODE must be 5 characters")
    private String portUnLocCode;
    
    @Min(value = 1, message = "Cargo amount must be at least 1")
    private int cargoAmount;
    
    @NotBlank(message = "Movement type is required (ARRIVAL or DEPARTURE)")
    private String movementType; // "ARRIVAL" or "DEPARTURE"
    
    // Construtores
    public RecordPortCargoMovementResource() {}
    
    public RecordPortCargoMovementResource(String portUnLocCode, int cargoAmount, String movementType) {
        this.portUnLocCode = portUnLocCode;
        this.cargoAmount = cargoAmount;
        this.movementType = movementType;
    }
    
    // Getters e Setters
    public String getPortUnLocCode() { return portUnLocCode; }
    public void setPortUnLocCode(String portUnLocCode) { this.portUnLocCode = portUnLocCode; }
    
    public int getCargoAmount() { return cargoAmount; }
    public void setCargoAmount(int cargoAmount) { this.cargoAmount = cargoAmount; }
    
    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }
}
