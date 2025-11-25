package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Location {
    @Column(name = "origin_id")
    private final String unLocCode;
    
    public Location() {
        this.unLocCode = null;
    }
    
    public Location(String unLocCode) {
        this.unLocCode = unLocCode;
    }
    
    public String getUnLocCode() { 
        return this.unLocCode; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return Objects.equals(unLocCode, location.unLocCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unLocCode);
    }
}