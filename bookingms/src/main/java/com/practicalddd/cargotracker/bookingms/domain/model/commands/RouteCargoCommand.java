package com.practicalddd.cargotracker.bookingms.domain.model.commands;

import java.time.LocalDateTime;

public class RouteCargoCommand {
    private final String cargoBookingId;
    private final LocalDateTime routingDeadline;

    public RouteCargoCommand(String cargoBookingId) {
        this(cargoBookingId, LocalDateTime.now().plusDays(7));
    }
    
    public RouteCargoCommand(String cargoBookingId, LocalDateTime routingDeadline) {
        if (cargoBookingId == null || cargoBookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Cargo booking ID cannot be null or empty");
        }
        
        // Validação mais permissiva para compatibilidade
        String normalizedId = cargoBookingId.trim().toUpperCase();
        if (normalizedId.length() < 4) {
            throw new IllegalArgumentException("Cargo booking ID must be at least 4 characters");
        }
        
        if (!normalizedId.matches("^[A-Z0-9]+$")) {
            throw new IllegalArgumentException(
                "Cargo booking ID can only contain uppercase letters and numbers");
        }
        
        if (routingDeadline == null) {
            throw new IllegalArgumentException("Routing deadline cannot be null");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (routingDeadline.isBefore(now)) {
            throw new IllegalArgumentException("Routing deadline cannot be in the past");
        }
        
        if (routingDeadline.isBefore(now.plusHours(1))) {
            throw new IllegalArgumentException("Routing deadline must be at least 1 hour from now");
        }
        
        if (routingDeadline.isAfter(now.plusDays(30))) {
            throw new IllegalArgumentException("Routing deadline cannot be more than 30 days in the future");
        }
        
        this.cargoBookingId = normalizedId;
        this.routingDeadline = routingDeadline;
    }

    public String getCargoBookingId() {
        return cargoBookingId;
    }
    
    public LocalDateTime getRoutingDeadline() {
        return routingDeadline;
    }
}
