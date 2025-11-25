package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BookingId implements Serializable {
    @Column(name="booking_id")
    private final String bookingId;

    public BookingId() {
        this.bookingId = null;
    }

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