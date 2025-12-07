package com.practicalddd.cargotracker.handlingms.interfaces.rest.dto;

import java.time.LocalDateTime;

public class HandlingActivityRegistrationResource {

    private String bookingId;
    private String voyageNumber;
    private String unLocode;
    private String handlingType;
    private LocalDateTime completionTime;

    public HandlingActivityRegistrationResource(){}

    public HandlingActivityRegistrationResource(String bookingId, String voyageNumber, String unLocode, String handlingType, LocalDateTime completionTime){
        this.setBookingId(bookingId);
        this.setVoyageNumber(voyageNumber);
        this.setUnLocode(unLocode);
        this.setCompletionTime(completionTime);
    }


    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getVoyageNumber() {
        return voyageNumber;
    }

    public void setVoyageNumber(String voyageNumber) {
        this.voyageNumber = voyageNumber;
    }

    public String getUnLocode() {
        return unLocode;
    }

    public void setUnLocode(String unLocode) {
        this.unLocode = unLocode;
    }

    public String getHandlingType() {
        return handlingType;
    }

    public void setHandlingType(String handlingType) {
        this.handlingType = handlingType;
    }

    public LocalDateTime getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(LocalDateTime completionTime) {
        this.completionTime = completionTime;
    }
}
