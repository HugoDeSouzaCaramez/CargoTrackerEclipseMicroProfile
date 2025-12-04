package com.practicalddd.cargotracker.bookingms.interfaces.rest.dto;

public class CargoSummaryResource {
    private String bookingId;
    private String origin;
    private String destination;
    private String status;

    public CargoSummaryResource() {}

    public CargoSummaryResource(String bookingId, String origin, String destination, String status) {
        this.bookingId = bookingId;
        this.origin = origin;
        this.destination = destination;
        this.status = status;
    }

    // Getters e Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
