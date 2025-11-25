package com.practicalddd.cargotracker.bookingms.domain.model.commands;

public class RouteCargoCommand {
    private final String cargoBookingId;

    public RouteCargoCommand(String cargoBookingId) {
        this.cargoBookingId = cargoBookingId;
    }

    public String getCargoBookingId() {
        return cargoBookingId;
    }
}