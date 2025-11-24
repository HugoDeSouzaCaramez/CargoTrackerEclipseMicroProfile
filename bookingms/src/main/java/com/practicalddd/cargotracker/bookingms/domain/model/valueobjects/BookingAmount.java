package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class BookingAmount {

    @Column(name = "booking_amount", unique = true, updatable= false)
    private Integer bookingAmount;

    public BookingAmount(){}

    public BookingAmount(Integer bookingAmount){this.bookingAmount = bookingAmount;}

    public void setBookingAmount(Integer bookingAmount){this.bookingAmount = bookingAmount;}

    public Integer getBookingAmount(){return this.bookingAmount;}
}
