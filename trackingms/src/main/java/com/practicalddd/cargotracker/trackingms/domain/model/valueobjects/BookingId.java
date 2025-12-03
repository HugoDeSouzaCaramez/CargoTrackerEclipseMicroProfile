package com.practicalddd.cargotracker.trackingms.domain.model.valueobjects;

import java.util.Objects;

public class BookingId {
    
    private final String bookingId;

    public BookingId(String bookingId) {
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
        if (o == null || getClass() != o.getClass()) return false;
        BookingId that = (BookingId) o;
        return bookingId.equals(that.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }

    @Override
    public String toString() {
        return bookingId;
    }
}
