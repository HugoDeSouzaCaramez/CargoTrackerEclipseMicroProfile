package com.practicalddd.cargotracker.bookingms.domain.model.commands;

import java.time.LocalDateTime;

public class RouteCargoCommand {
    private final String cargoBookingId;
    private final LocalDateTime routingDeadline;  // ✅ Novo campo

    public RouteCargoCommand(String cargoBookingId) {
        this(cargoBookingId, LocalDateTime.now().plusDays(7)); // Default: 7 dias
    }
    
    public RouteCargoCommand(String cargoBookingId, LocalDateTime routingDeadline) {
        if (cargoBookingId == null || cargoBookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Cargo booking ID cannot be null or empty");
        }
        if (routingDeadline == null) {
            throw new IllegalArgumentException("Routing deadline cannot be null");
        }
        if (routingDeadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Routing deadline cannot be in the past");
        }
        
        this.cargoBookingId = cargoBookingId;
        this.routingDeadline = routingDeadline;
    }

    public String getCargoBookingId() {
        return cargoBookingId;
    }
    
    public LocalDateTime getRoutingDeadline() {
        return routingDeadline;
    }
}
