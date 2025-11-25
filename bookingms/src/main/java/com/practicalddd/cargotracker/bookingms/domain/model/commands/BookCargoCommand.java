package com.practicalddd.cargotracker.bookingms.domain.model.commands;

import java.util.Date;

public class BookCargoCommand {
    private final String bookingId;
    private final int bookingAmount;
    private final String originLocation;
    private final String destLocation;
    private final Date destArrivalDeadline;

    public BookCargoCommand(String bookingId, int bookingAmount,
                           String originLocation, String destLocation, Date destArrivalDeadline) {
        this.bookingId = bookingId;
        this.bookingAmount = bookingAmount;
        this.originLocation = originLocation;
        this.destLocation = destLocation;
        this.destArrivalDeadline = new Date(destArrivalDeadline.getTime());
    }

    public BookCargoCommand(int bookingAmount,
                           String originLocation, String destLocation, Date destArrivalDeadline) {
        this(null, bookingAmount, originLocation, destLocation, destArrivalDeadline);
    }

    public String getBookingId() { return this.bookingId; }
    public int getBookingAmount() { return this.bookingAmount; }
    public String getOriginLocation() { return originLocation; }
    public String getDestLocation() { return destLocation; }
    public Date getDestArrivalDeadline() { return new Date(destArrivalDeadline.getTime()); }
}