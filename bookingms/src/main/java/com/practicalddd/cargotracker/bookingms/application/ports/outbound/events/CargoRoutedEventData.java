package com.practicalddd.cargotracker.bookingms.application.ports.outbound.events;

public class CargoRoutedEventData {
    private String bookingId;

    public CargoRoutedEventData(){}

    public void setBookingId(String bookingId){
        this.bookingId = bookingId;
    }

    public String getBookingId(){return this.bookingId;}
}
