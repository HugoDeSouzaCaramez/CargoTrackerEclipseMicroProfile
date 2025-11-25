package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import java.util.Objects;

public class BookingAmount {
    private final Integer bookingAmount;

    public BookingAmount(Integer bookingAmount) {
        this.bookingAmount = bookingAmount;
    }

    public Integer getBookingAmount() { 
        return this.bookingAmount; 
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
}