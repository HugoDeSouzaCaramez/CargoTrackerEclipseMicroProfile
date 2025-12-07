package com.practicalddd.cargotracker.bookingms.domain.model.entities;

import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Voyage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Leg {
    private final Voyage voyage;
    private final Location loadLocation;
    private final Location unloadLocation;
    private final LocalDateTime loadTime;
    private final LocalDateTime unloadTime;

    public Leg(Voyage voyage, Location loadLocation,
               Location unloadLocation, LocalDateTime loadTime, LocalDateTime unloadTime) {
        
        validateLeg(voyage, loadLocation, unloadLocation, loadTime, unloadTime);
        
        this.voyage = voyage;
        this.loadLocation = loadLocation;
        this.unloadLocation = unloadLocation;
        this.loadTime = loadTime;
        this.unloadTime = unloadTime;
    }

    private void validateLeg(Voyage voyage, Location loadLocation, Location unloadLocation,
                            LocalDateTime loadTime, LocalDateTime unloadTime) {
        if (voyage == null) {
            throw new IllegalArgumentException("Voyage cannot be null");
        }
        if (loadLocation == null) {
            throw new IllegalArgumentException("Load location cannot be null");
        }
        if (unloadLocation == null) {
            throw new IllegalArgumentException("Unload location cannot be null");
        }
        if (loadTime == null) {
            throw new IllegalArgumentException("Load time cannot be null");
        }
        if (unloadTime == null) {
            throw new IllegalArgumentException("Unload time cannot be null");
        }
        if (loadTime.isAfter(unloadTime)) {
            throw new IllegalArgumentException("Load time must be before unload time");
        }
        if (loadLocation.equals(unloadLocation)) {
            throw new IllegalArgumentException("Load and unload locations must be different");
        }
    }

    // Business methods
    public long getDurationInHours() {
        return ChronoUnit.HOURS.between(loadTime, unloadTime);
    }
    
    public boolean isWithinTimeFrame(LocalDateTime start, LocalDateTime end) {
        return !loadTime.isBefore(start) && !unloadTime.isAfter(end);
    }

    // Getters
    public Voyage getVoyage() { 
        return voyage; 
    }
    
    public Location getLoadLocation() { 
        return loadLocation; 
    }
    
    public Location getUnloadLocation() { 
        return unloadLocation; 
    }
    
    public LocalDateTime getLoadTime() { 
        return loadTime; 
    }
    
    public LocalDateTime getUnloadTime() { 
        return unloadTime; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Leg)) return false;
        Leg leg = (Leg) o;
        return Objects.equals(voyage, leg.voyage) &&
               Objects.equals(loadLocation, leg.loadLocation) &&
               Objects.equals(unloadLocation, leg.unloadLocation) &&
               Objects.equals(loadTime, leg.loadTime) &&
               Objects.equals(unloadTime, leg.unloadTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voyage, loadLocation, unloadLocation, loadTime, unloadTime);
    }

    @Override
    public String toString() {
        return "Leg{" +
                "voyage=" + voyage +
                ", loadLocation=" + loadLocation +
                ", unloadLocation=" + unloadLocation +
                ", loadTime=" + loadTime +
                ", unloadTime=" + unloadTime +
                '}';
    }
}
