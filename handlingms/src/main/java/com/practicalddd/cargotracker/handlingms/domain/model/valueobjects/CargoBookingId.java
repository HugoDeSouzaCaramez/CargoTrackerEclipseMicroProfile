package com.practicalddd.cargotracker.handlingms.domain.model.valueobjects;

import java.util.Objects;

public class CargoBookingId {
    private final String bookingId;
    
    public CargoBookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be null or empty");
        }
        this.bookingId = bookingId;
    }
    
    public String getBookingId() {
        return this.bookingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CargoBookingId)) return false;
        CargoBookingId that = (CargoBookingId) o;
        return Objects.equals(bookingId, that.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }

    @Override
    public String toString() {
        return "CargoBookingId{" +
                "bookingId='" + bookingId + '\'' +
                '}';
    }
}
