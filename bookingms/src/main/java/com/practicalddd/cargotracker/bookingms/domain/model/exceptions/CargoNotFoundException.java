package com.practicalddd.cargotracker.bookingms.domain.model.exceptions;

public class CargoNotFoundException extends DomainException {
    public CargoNotFoundException(String bookingId) {
        super(String.format("Cargo with booking id %s not found", bookingId));
    }
}