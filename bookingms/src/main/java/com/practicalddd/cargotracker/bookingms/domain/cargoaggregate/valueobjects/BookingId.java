package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

public class BookingId {
    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Z0-9]{4,20}$");
    private final String bookingId;

    public BookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be null or empty");
        }
        
        String normalized = bookingId.trim().toUpperCase();
        
        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Invalid booking ID format. Must be 4-20 characters, uppercase letters and numbers only"
            );
        }
        
        this.bookingId = normalized;
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
    
    @Override
    public String toString() {
        return bookingId;
    }
}
