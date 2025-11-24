package com.practicalddd.cargotracker.shareddomain.events;

public class CargoHandledEventData {
    private String bookingId;
    private String handlingType;

    public CargoHandledEventData(String bookingId,String handlingType){
        this.bookingId = bookingId;
        this.handlingType = handlingType;
    }

    public String getBookingId(){return this.bookingId;}
    public String getHandlingType(){return this.handlingType;}
}
