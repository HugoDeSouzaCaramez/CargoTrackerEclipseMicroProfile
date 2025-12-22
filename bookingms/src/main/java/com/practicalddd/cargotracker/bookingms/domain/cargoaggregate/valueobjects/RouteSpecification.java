package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.entities.Leg;
import com.practicalddd.cargotracker.bookingms.domain.services.RouteFeasibilityService;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class RouteSpecification {
    
    @Inject
    private transient RouteFeasibilityService routeFeasibilityService;
    
    private final Location origin;
    private final Location destination;
    private final LocalDateTime arrivalDeadline;
    private final TransportMode transportMode;
    private final CargoType cargoType;
    private final boolean isIntercontinental;

    public RouteSpecification(Location origin, Location destination, LocalDateTime arrivalDeadline) {
        this(origin, destination, arrivalDeadline, TransportMode.SEA, CargoType.GENERAL);
    }
    
    public RouteSpecification(Location origin, Location destination, 
                             LocalDateTime arrivalDeadline, TransportMode transportMode, CargoType cargoType) {
        validateSpecification(origin, destination, arrivalDeadline);
        
        this.origin = origin;
        this.destination = destination;
        this.arrivalDeadline = arrivalDeadline;
        this.transportMode = transportMode != null ? transportMode : TransportMode.SEA;
        this.cargoType = cargoType != null ? cargoType : CargoType.GENERAL;
        this.isIntercontinental = isIntercontinental(origin, destination);
    }

    private void validateSpecification(Location origin, Location destination, LocalDateTime arrivalDeadline) {
        if (origin == null) {
            throw new IllegalArgumentException("Origin cannot be null");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination cannot be null");
        }
        if (arrivalDeadline == null) {
            throw new IllegalArgumentException("Arrival deadline cannot be null");
        }
        
        // Usar DeadlinePolicy para validação consistente
        boolean isIntercontinental = isIntercontinental(origin, destination);
        DeadlinePolicy policy = new DeadlinePolicy(arrivalDeadline, isIntercontinental);
        
        if (!policy.isValidForBooking()) {
            throw new IllegalArgumentException(
                String.format("Prazo insuficiente para rota %s. Mínimo requerido: %s. Dias restantes: %d",
                    isIntercontinental ? "intercontinental" : "regional",
                    policy.getMinimumRequiredDays(),
                    policy.getRemainingDays())
            );
        }
        
        if (origin.equals(destination)) {
            throw new IllegalArgumentException("Origin and destination must be different");
        }
        
        // Aviso se for urgente
        if (policy.isUrgent()) {
            System.out.println(String.format(
                "[AVISO] Rota %s criada com status %s. Prazo: %d dias",
                isIntercontinental ? "intercontinental" : "regional",
                policy.getPriorityCategory(),
                policy.getRemainingDays()
            ));
        }
    }
    
    private boolean isIntercontinental(Location origin, Location destination) {
        String originContinent = getContinentCode(origin.getUnLocCode());
        String destContinent = getContinentCode(destination.getUnLocCode());
        return !originContinent.equals(destContinent);
    }
    
    private String getContinentCode(String unlocode) {
        // Extrai código de continente baseado no código de país
        String countryCode = unlocode.substring(0, 2);
        
        // Mapeamento simplificado de países para continentes
        switch (countryCode) {
            case "US": case "CA": case "MX": return "NA"; // América do Norte
            case "BR": case "AR": case "CL": return "SA"; // América do Sul
            case "GB": case "DE": case "FR": case "NL": case "ES": case "IT": return "EU"; // Europa
            case "CN": case "JP": case "KR": case "IN": return "AS"; // Ásia
            case "AU": case "NZ": return "OC"; // Oceania
            case "ZA": case "EG": case "NG": return "AF"; // África
            default: return "XX"; // Desconhecido
        }
    }

    public boolean isSatisfiedBy(CargoItinerary itinerary) {
        if (itinerary == null || itinerary.isEmpty()) {
            return false;
        }
        
        List<Leg> legs = itinerary.getLegs();
        Leg firstLeg = legs.get(0);
        Leg lastLeg = legs.get(legs.size() - 1);
        
        if (!firstLeg.getLoadLocation().equals(origin)) {
            return false;
        }
        
        if (!lastLeg.getUnloadLocation().equals(destination)) {
            return false;
        }
        
        LocalDateTime estimatedArrival = lastLeg.getUnloadTime();
        if (estimatedArrival.isAfter(arrivalDeadline)) {
            return false;
        }
        
        if (!isTransportModeCompatible(itinerary)) {
            return false;
        }
        
        return true;
    }
    
    private boolean isTransportModeCompatible(CargoItinerary itinerary) {
        return itinerary.getLegs().stream()
            .allMatch(leg -> isLegCompatibleWithTransportMode(leg));
    }
    
    private boolean isLegCompatibleWithTransportMode(Leg leg) {
        // Lógica para validar compatibilidade
        return true;
    }
    
    public long getRemainingDays() {
        return ChronoUnit.DAYS.between(LocalDateTime.now(), arrivalDeadline);
    }
    
    public boolean isUrgent() {
        DeadlinePolicy policy = new DeadlinePolicy(arrivalDeadline, isIntercontinental);
        return policy.isUrgent();
    }
    
    public boolean isCritical() {
        DeadlinePolicy policy = new DeadlinePolicy(arrivalDeadline, isIntercontinental);
        return policy.isCritical();
    }
    
    public String getPriorityCategory() {
        DeadlinePolicy policy = new DeadlinePolicy(arrivalDeadline, isIntercontinental);
        return policy.getPriorityCategory();
    }
    
    public boolean isIntercontinental() {
        return isIntercontinental;
    }

    // Getters
    public Location getOrigin() { return origin; }
    public Location getDestination() { return destination; }
    public LocalDateTime getArrivalDeadline() { return arrivalDeadline; }
    public TransportMode getTransportMode() { return transportMode; }
    public CargoType getCargoType() { return cargoType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteSpecification)) return false;
        RouteSpecification that = (RouteSpecification) o;
        return Objects.equals(origin, that.origin) &&
               Objects.equals(destination, that.destination) &&
               Objects.equals(arrivalDeadline, that.arrivalDeadline) &&
               transportMode == that.transportMode &&
               cargoType == that.cargoType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination, arrivalDeadline, transportMode, cargoType);
    }

    @Override
    public String toString() {
        return "RouteSpecification{" +
                "origin=" + origin +
                ", destination=" + destination +
                ", arrivalDeadline=" + arrivalDeadline +
                ", transportMode=" + transportMode +
                ", cargoType=" + cargoType +
                ", isIntercontinental=" + isIntercontinental +
                '}';
    }
}
