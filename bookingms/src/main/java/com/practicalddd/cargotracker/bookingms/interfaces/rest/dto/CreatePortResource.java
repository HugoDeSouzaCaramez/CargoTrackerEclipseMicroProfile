package com.practicalddd.cargotracker.bookingms.interfaces.rest.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreatePortResource {
    
    @NotBlank(message = "UN/LOCODE is required")
    @Size(min = 5, max = 5, message = "UN/LOCODE must be 5 characters")
    private String unLocCode;
    
    @NotBlank(message = "Port name is required")
    @Size(min = 2, max = 100, message = "Port name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters")
    private String country;
    
    private String timeZone;
    
    @Min(value = 1, message = "Initial capacity must be at least 1")
    private int initialCapacity;
    
    // Construtores
    public CreatePortResource() {}
    
    public CreatePortResource(String unLocCode, String name, String country, 
                            String timeZone, int initialCapacity) {
        this.unLocCode = unLocCode;
        this.name = name;
        this.country = country;
        this.timeZone = timeZone;
        this.initialCapacity = initialCapacity;
    }
    
    // Getters e Setters
    public String getUnLocCode() { return unLocCode; }
    public void setUnLocCode(String unLocCode) { this.unLocCode = unLocCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    
    public int getInitialCapacity() { return initialCapacity; }
    public void setInitialCapacity(int initialCapacity) { this.initialCapacity = initialCapacity; }
}
