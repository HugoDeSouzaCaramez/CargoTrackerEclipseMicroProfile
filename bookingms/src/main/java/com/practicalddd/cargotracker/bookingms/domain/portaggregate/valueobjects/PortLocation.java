package com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects;

import java.util.Objects;

public class PortLocation {
    private final String unLocCode;
    private final String name;
    private final String country;
    private final String timeZone;

    public PortLocation(String unLocCode, String name, String country, String timeZone) {
        validateLocation(unLocCode, name, country);
        
        this.unLocCode = unLocCode.toUpperCase();
        this.name = name;
        this.country = country;
        this.timeZone = timeZone != null ? timeZone : "UTC";
    }

    private void validateLocation(String unLocCode, String name, String country) {
        if (unLocCode == null || unLocCode.trim().isEmpty()) {
            throw new IllegalArgumentException("UN/LOCODE cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Port name cannot be null or empty");
        }
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
    }

    // Getters
    public String getUnLocCode() { return unLocCode; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getTimeZone() { return timeZone; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortLocation)) return false;
        PortLocation that = (PortLocation) o;
        return Objects.equals(unLocCode, that.unLocCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unLocCode);
    }
}
