package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import java.util.Objects;

public class BookingAmount {
    private static final int MIN_AMOUNT = 1;
    private static final int MAX_AMOUNT = 1000000; // 1 milhão como limite superior
    
    private final Integer bookingAmount;

    public BookingAmount(Integer bookingAmount) {
        if (bookingAmount == null) {
            throw new IllegalArgumentException("Booking amount cannot be null");
        }
        
        if (bookingAmount < MIN_AMOUNT) {
            throw new IllegalArgumentException(
                String.format("Booking amount must be at least %d", MIN_AMOUNT)
            );
        }
        
        if (bookingAmount > MAX_AMOUNT) {
            throw new IllegalArgumentException(
                String.format("Booking amount cannot exceed %d", MAX_AMOUNT)
            );
        }
        
        this.bookingAmount = bookingAmount;
    }

    public Integer getBookingAmount() { 
        return this.bookingAmount; 
    }
    
    public BookingAmount add(BookingAmount other) {
        if (other == null) return this;
        
        int newAmount = this.bookingAmount + other.getBookingAmount();
        return new BookingAmount(newAmount);
    }
    
    public BookingAmount subtract(BookingAmount other) {
        if (other == null) return this;
        
        int newAmount = this.bookingAmount - other.getBookingAmount();
        return new BookingAmount(newAmount);
    }
    
    public boolean isGreaterThan(BookingAmount other) {
        if (other == null) return true;
        return this.bookingAmount > other.getBookingAmount();
    }
    
    public boolean isZeroOrNegative() {
        return this.bookingAmount <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingAmount)) return false;
        BookingAmount that = (BookingAmount) o;
        return Objects.equals(bookingAmount, that.bookingAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingAmount);
    }
    
    @Override
    public String toString() {
        return bookingAmount.toString();
    }
}
