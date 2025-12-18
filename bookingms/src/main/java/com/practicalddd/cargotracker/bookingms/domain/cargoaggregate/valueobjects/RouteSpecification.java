package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.entities.Leg;

public class RouteSpecification {
    private final Location origin;
    private final Location destination;
    private final LocalDateTime arrivalDeadline;
    private final TransportMode transportMode;
    private final CargoType cargoType;

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
        if (arrivalDeadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Arrival deadline cannot be in the past");
        }
        if (origin.equals(destination)) {
            throw new IllegalArgumentException("Origin and destination must be different");
        }
        
        // Validar prazo mínimo baseado na distância (simplificado)
        LocalDateTime minimumDeadline = LocalDateTime.now().plusDays(calculateMinimumDays(origin, destination));
        if (arrivalDeadline.isBefore(minimumDeadline)) {
            throw new IllegalArgumentException(
                String.format("Arrival deadline too soon. Minimum required: %s", minimumDeadline)
            );
        }
    }
    
    private long calculateMinimumDays(Location origin, Location destination) {
        // Simulação: distâncias baseadas em regiões
        // Usar serviço de geolocalização no futuro
        boolean intercontinental = isIntercontinental(origin, destination);
        return intercontinental ? 14L : 7L; // 14 dias intercontinental, 7 dias regional
    }
    
    private boolean isIntercontinental(Location origin, Location destination) {
        // Lógica simplificada para determinar se é viagem intercontinental
        String originContinent = getContinentCode(origin.getUnLocCode());
        String destContinent = getContinentCode(destination.getUnLocCode());
        return !originContinent.equals(destContinent);
    }
    
    private String getContinentCode(String unlocode) {
        // Extrai código de continente do UN/LOCODE (primeira letra)
        return unlocode.substring(0, 1);
    }

    public boolean isSatisfiedBy(CargoItinerary itinerary) {
        if (itinerary == null || itinerary.isEmpty()) {
            return false;
        }
        
        // Verificar se o itinerário conecta origem e destino
        List<Leg> legs = itinerary.getLegs();
        Leg firstLeg = legs.get(0);
        Leg lastLeg = legs.get(legs.size() - 1);
        
        if (!firstLeg.getLoadLocation().equals(origin)) {
            return false;
        }
        
        if (!lastLeg.getUnloadLocation().equals(destination)) {
            return false;
        }
        
        // Verificar se chega antes do deadline
        LocalDateTime estimatedArrival = lastLeg.getUnloadTime();
        if (estimatedArrival.isAfter(arrivalDeadline)) {
            return false;
        }
        
        // Verificar compatibilidade com modo de transporte
        if (!isTransportModeCompatible(itinerary)) {
            return false;
        }
        
        return true;
    }
    
    private boolean isTransportModeCompatible(CargoItinerary itinerary) {
        // Verificar se todos os legs são compatíveis com o modo de transporte
        return itinerary.getLegs().stream()
            .allMatch(leg -> isLegCompatibleWithTransportMode(leg));
    }
    
    private boolean isLegCompatibleWithTransportMode(Leg leg) {
        // Lógica para validar compatibilidade (simplificada)
        // No futuro, verificar tipo de vessel vs. transport mode
        return true;
    }
    
    public long getRemainingDays() {
        return ChronoUnit.DAYS.between(LocalDateTime.now(), arrivalDeadline);
    }
    
    public boolean isUrgent() {
        return getRemainingDays() < 7;
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
                '}';
    }
}
