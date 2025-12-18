package com.practicalddd.cargotracker.bookingms.interfaces.rest.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UpdatePortCapacityResource {
    
    @NotBlank(message = "Port UN/LOCODE is required")
    @Size(min = 5, max = 5, message = "UN/LOCODE must be 5 characters")
    private String portUnLocCode;
    
    @Min(value = 1, message = "New max capacity must be at least 1")
    private int newMaxCapacity;
    
    // Construtores
    public UpdatePortCapacityResource() {}
    
    public UpdatePortCapacityResource(String portUnLocCode, int newMaxCapacity) {
        this.portUnLocCode = portUnLocCode;
        this.newMaxCapacity = newMaxCapacity;
    }
    
    // Getters e Setters
    public String getPortUnLocCode() { return portUnLocCode; }
    public void setPortUnLocCode(String portUnLocCode) { this.portUnLocCode = portUnLocCode; }
    
    public int getNewMaxCapacity() { return newMaxCapacity; }
    public void setNewMaxCapacity(int newMaxCapacity) { this.newMaxCapacity = newMaxCapacity; }
}
