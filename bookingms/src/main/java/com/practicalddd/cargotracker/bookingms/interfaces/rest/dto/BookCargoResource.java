package com.practicalddd.cargotracker.bookingms.interfaces.rest.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class BookCargoResource {

    @Min(value = 1, message = "Booking amount must be at least 1")
    private int bookingAmount;

    @NotBlank(message = "Origin location is required")
    @Size(min = 2, max = 10, message = "Origin location must be between 2 and 10 characters")
    private String originLocation;

    @NotBlank(message = "Destination location is required")
    @Size(min = 2, max = 10, message = "Destination location must be between 2 and 10 characters")
    private String destLocation;

    @NotNull(message = "Arrival deadline is required")
    @Future(message = "Arrival deadline must be in the future")
    private LocalDateTime destArrivalDeadline;

    public BookCargoResource() {
    }

    public BookCargoResource(int bookingAmount,
            String originLocation, String destLocation, LocalDateTime destArrivalDeadline) {

        this.bookingAmount = bookingAmount;
        this.originLocation = originLocation;
        this.destLocation = destLocation;
        this.destArrivalDeadline = destArrivalDeadline;
    }

    public void setBookingAmount(int bookingAmount) {
        this.bookingAmount = bookingAmount;
    }

    public int getBookingAmount() {
        return this.bookingAmount;
    }

    public String getOriginLocation() {
        return originLocation;
    }

    public void setOriginLocation(String originLocation) {
        this.originLocation = originLocation;
    }

    public String getDestLocation() {
        return destLocation;
    }

    public void setDestLocation(String destLocation) {
        this.destLocation = destLocation;
    }

    public LocalDateTime getDestArrivalDeadline() {
        return destArrivalDeadline;
    }

    public void setDestArrivalDeadline(LocalDateTime destArrivalDeadline) {
        this.destArrivalDeadline = destArrivalDeadline;
    }

}
