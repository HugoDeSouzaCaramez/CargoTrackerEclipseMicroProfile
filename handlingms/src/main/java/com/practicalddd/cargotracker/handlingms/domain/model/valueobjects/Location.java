package com.practicalddd.cargotracker.handlingms.domain.model.valueobjects;

import java.util.Objects;

public class Location {
    private final String unLocCode;
    
    public Location(String unLocCode) {
        if (unLocCode == null || unLocCode.trim().isEmpty()) {
            throw new IllegalArgumentException("UN/Locode cannot be null or empty");
        }
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

    @Override
    public String toString() {
        return "Location{" +
                "unLocCode='" + unLocCode + '\'' +
                '}';
    }
}
