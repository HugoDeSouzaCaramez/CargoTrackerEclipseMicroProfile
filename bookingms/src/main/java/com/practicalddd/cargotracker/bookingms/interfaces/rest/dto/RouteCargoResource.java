package com.practicalddd.cargotracker.bookingms.interfaces.rest.dto;


public class RouteCargoResource {

    private String bookingId;


    public RouteCargoResource(){}

    public RouteCargoResource(String bookingId){
        this.bookingId = bookingId;
    }

    public String getBookingId(){return this.bookingId;}

    public void setBookingId(String bookingId){this.bookingId = bookingId;}
}
