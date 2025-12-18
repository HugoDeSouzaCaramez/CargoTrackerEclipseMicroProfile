package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.entities;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.Location;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.TransportMode;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.Voyage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Leg {
    private final Voyage voyage;
    private final Location loadLocation;
    private final Location unloadLocation;
    private final LocalDateTime loadTime;
    private final LocalDateTime unloadTime;
    private final TransportMode transportMode;

    public Leg(Voyage voyage, Location loadLocation,
               Location unloadLocation, LocalDateTime loadTime, LocalDateTime unloadTime) {
        this(voyage, loadLocation, unloadLocation, loadTime, unloadTime, TransportMode.SEA);
    }
    
    public Leg(Voyage voyage, Location loadLocation,
               Location unloadLocation, LocalDateTime loadTime, LocalDateTime unloadTime,
               TransportMode transportMode) {
        
        validateLeg(voyage, loadLocation, unloadLocation, loadTime, unloadTime, transportMode);
        
        this.voyage = voyage;
        this.loadLocation = loadLocation;
        this.unloadLocation = unloadLocation;
        this.loadTime = loadTime;
        this.unloadTime = unloadTime;
        this.transportMode = transportMode;
    }

    private void validateLeg(Voyage voyage, Location loadLocation, Location unloadLocation,
                            LocalDateTime loadTime, LocalDateTime unloadTime, TransportMode transportMode) {
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
        if (transportMode == null) {
            throw new IllegalArgumentException("Transport mode cannot be null");
        }
        
        if (loadTime.isAfter(unloadTime)) {
            throw new IllegalArgumentException("Load time must be before unload time");
        }
        
        if (loadLocation.equals(unloadLocation)) {
            throw new IllegalArgumentException("Load and unload locations must be different");
        }
        
        // Validar duração máxima baseada no modo de transporte
        long durationHours = ChronoUnit.HOURS.between(loadTime, unloadTime);
        validateDuration(durationHours, transportMode);
        
        // Validar compatibilidade de localização com modo de transporte
        validateLocationCompatibility(loadLocation, unloadLocation, transportMode);
    }
    
    private void validateDuration(long durationHours, TransportMode transportMode) {
        long maxDuration;
        
        switch (transportMode) {
            case AIR:
                maxDuration = 48;    // 48 horas máximo para voos
                break;
            case SEA:
                maxDuration = 720;   // 30 dias máximo para navegação
                break;
            case LAND:
                maxDuration = 168;   // 7 dias máximo para transporte terrestre
                break;
            case RAIL:
                maxDuration = 240;   // 10 dias máximo para ferroviário
                break;
            default:
                maxDuration = 744;   // 31 dias para outros
                break;
        }
        
        if (durationHours > maxDuration) {
            throw new IllegalArgumentException(
                String.format("Duration exceeds maximum allowed for %s transport: %d hours", 
                transportMode, maxDuration)
            );
        }
    }
    
    private void validateLocationCompatibility(Location loadLoc, Location unloadLoc, TransportMode mode) {
        switch(mode) {
            case SEA:
                if (!loadLoc.isSeaport() || !unloadLoc.isSeaport()) {
                    throw new IllegalArgumentException("Sea transport requires seaport locations");
                }
                break;
            case AIR:
                if (!loadLoc.isAirport() || !unloadLoc.isAirport()) {
                    throw new IllegalArgumentException("Air transport requires airport locations");
                }
                break;
            // Outras validações podem ser adicionadas
            default:
                // Para outros modos de transporte, não há validação específica
                break;
        }
    }

    // Business methods
    public long getDurationInHours() {
        return ChronoUnit.HOURS.between(loadTime, unloadTime);
    }
    
    public boolean isWithinTimeFrame(LocalDateTime start, LocalDateTime end) {
        return !loadTime.isBefore(start) && !unloadTime.isAfter(end);
    }
    
    public boolean isActiveAt(LocalDateTime timestamp) {
        return !loadTime.isAfter(timestamp) && !unloadTime.isBefore(timestamp);
    }
    
    public boolean isDirect() {
        // Um leg é considerado direto se não houver escalas intermediárias
        // (simplificação, verificar no objeto Voyage)
        return true;
    }
    
    public boolean hasVessel() {
        return voyage != null && voyage.getVoyageNumber() != null;
    }

    // Getters
    public Voyage getVoyage() { return voyage; }
    public Location getLoadLocation() { return loadLocation; }
    public Location getUnloadLocation() { return unloadLocation; }
    public LocalDateTime getLoadTime() { return loadTime; }
    public LocalDateTime getUnloadTime() { return unloadTime; }
    public TransportMode getTransportMode() { return transportMode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Leg)) return false;
        Leg leg = (Leg) o;
        return Objects.equals(voyage, leg.voyage) &&
               Objects.equals(loadLocation, leg.loadLocation) &&
               Objects.equals(unloadLocation, leg.unloadLocation) &&
               Objects.equals(loadTime, leg.loadTime) &&
               Objects.equals(unloadTime, leg.unloadTime) &&
               transportMode == leg.transportMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(voyage, loadLocation, unloadLocation, loadTime, unloadTime, transportMode);
    }

    @Override
    public String toString() {
        return "Leg{" +
                "voyage=" + voyage +
                ", loadLocation=" + loadLocation +
                ", unloadLocation=" + unloadLocation +
                ", loadTime=" + loadTime +
                ", unloadTime=" + unloadTime +
                ", transportMode=" + transportMode +
                ", duration=" + getDurationInHours() + "h" +
                '}';
    }
}
