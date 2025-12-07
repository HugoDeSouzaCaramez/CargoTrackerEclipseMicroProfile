package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import java.time.LocalDateTime;
import java.util.Objects;

public class RouteSpecification {
    private final Location origin;
    private final Location destination;
    private final LocalDateTime arrivalDeadline;

    public RouteSpecification(Location origin, Location destination, LocalDateTime arrivalDeadline) {
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
        
        this.origin = origin;
        this.destination = destination;
        this.arrivalDeadline = arrivalDeadline;
    }

    public Location getOrigin() {
        return origin;
    }

    public Location getDestination() {
        return destination;
    }

    public LocalDateTime getArrivalDeadline() {
        return arrivalDeadline;
    }

    public boolean isSatisfiedBy(CargoItinerary itinerary) {
        if (itinerary == null || itinerary.isEmpty()) {
            return false;
        }
        
        // Lógica de negócio: verificar se o itinerário atende à especificação
        // (simplificado para exemplo)
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteSpecification)) return false;
        RouteSpecification that = (RouteSpecification) o;
        return Objects.equals(origin, that.origin) &&
               Objects.equals(destination, that.destination) &&
               Objects.equals(arrivalDeadline, that.arrivalDeadline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination, arrivalDeadline);
    }

    @Override
    public String toString() {
        return "RouteSpecification{" +
                "origin=" + origin +
                ", destination=" + destination +
                ", arrivalDeadline=" + arrivalDeadline +
                '}';
    }
}
