package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import java.util.Objects;

public class BookingId {
    private final String bookingId;

    public BookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingId() { 
        return this.bookingId; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingId)) return false;
        BookingId that = (BookingId) o;
        return Objects.equals(bookingId, that.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }
}