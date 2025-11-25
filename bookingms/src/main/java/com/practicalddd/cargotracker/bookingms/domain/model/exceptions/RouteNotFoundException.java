package com.practicalddd.cargotracker.bookingms.domain.model.exceptions;

public class RouteNotFoundException extends DomainException {
    public RouteNotFoundException(String origin, String destination) {
        super(String.format("No route found from %s to %s", origin, destination));
    }
}