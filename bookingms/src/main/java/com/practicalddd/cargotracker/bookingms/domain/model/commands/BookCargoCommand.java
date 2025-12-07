package com.practicalddd.cargotracker.bookingms.domain.model.commands;

import java.time.LocalDateTime;
import java.util.Objects;

public class BookCargoCommand {
    private final String bookingId;
    private final int bookingAmount;
    private final String originLocation;
    private final String destLocation;
    private final LocalDateTime destArrivalDeadline;

    public BookCargoCommand(String bookingId, int bookingAmount,
                           String originLocation, String destLocation, 
                           LocalDateTime destArrivalDeadline) {
        
        validateCommand(bookingAmount, originLocation, destLocation, destArrivalDeadline);
        
        this.bookingId = bookingId;
        this.bookingAmount = bookingAmount;
        this.originLocation = originLocation;
        this.destLocation = destLocation;
        this.destArrivalDeadline = destArrivalDeadline;
    }

    public BookCargoCommand(int bookingAmount,
                           String originLocation, String destLocation, 
                           LocalDateTime destArrivalDeadline) {
        this(null, bookingAmount, originLocation, destLocation, destArrivalDeadline);
    }

    private void validateCommand(int bookingAmount, String originLocation, 
                                String destLocation, LocalDateTime destArrivalDeadline) {
        if (bookingAmount <= 0) {
            throw new IllegalArgumentException("Booking amount must be positive");
        }
        if (originLocation == null || originLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin location cannot be null or empty");
        }
        if (destLocation == null || destLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination location cannot be null or empty");
        }
        if (destArrivalDeadline == null) {
            throw new IllegalArgumentException("Arrival deadline cannot be null");
        }
        if (destArrivalDeadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Arrival deadline cannot be in the past");
        }
        if (originLocation.equals(destLocation)) {
            throw new IllegalArgumentException("Origin and destination must be different");
        }
    }

    // Getters
    public String getBookingId() { 
        return this.bookingId; 
    }
    
    public int getBookingAmount() { 
        return this.bookingAmount; 
    }
    
    public String getOriginLocation() { 
        return originLocation; 
    }
    
    public String getDestLocation() { 
        return destLocation; 
    }
    
    public LocalDateTime getDestArrivalDeadline() { 
        return destArrivalDeadline; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookCargoCommand)) return false;
        BookCargoCommand that = (BookCargoCommand) o;
        return bookingAmount == that.bookingAmount &&
               Objects.equals(bookingId, that.bookingId) &&
               Objects.equals(originLocation, that.originLocation) &&
               Objects.equals(destLocation, that.destLocation) &&
               Objects.equals(destArrivalDeadline, that.destArrivalDeadline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, bookingAmount, originLocation, destLocation, destArrivalDeadline);
    }

    @Override
    public String toString() {
        return "BookCargoCommand{" +
                "bookingId='" + bookingId + '\'' +
                ", bookingAmount=" + bookingAmount +
                ", originLocation='" + originLocation + '\'' +
                ", destLocation='" + destLocation + '\'' +
                ", destArrivalDeadline=" + destArrivalDeadline +
                '}';
    }
}
