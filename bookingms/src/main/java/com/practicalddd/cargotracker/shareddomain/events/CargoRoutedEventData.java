package com.practicalddd.cargotracker.shareddomain.events;

public class CargoRoutedEventData {

    private String bookingId;

    public CargoRoutedEventData(){}

    public void setBookingId(String bookingId){
        this.bookingId = bookingId;
    }

    public String getBookingId(){return this.bookingId;}

}
