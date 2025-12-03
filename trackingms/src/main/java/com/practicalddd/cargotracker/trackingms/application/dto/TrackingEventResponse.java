package com.practicalddd.cargotracker.trackingms.application.dto;

import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingEvent;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbProperty;
import java.util.Date;

public class TrackingEventResponse {
    
    @JsonbProperty("eventType")
    private String eventType;
    
    @JsonbProperty("eventTime")
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date eventTime;
    
    @JsonbProperty("location")
    private String location;
    
    @JsonbProperty("voyageNumber")
    private String voyageNumber;
    
    @JsonbProperty("description")
    private String description;
    
    @JsonbProperty("isMilestone")
    private boolean isMilestone;
    
    // Construtores
    public TrackingEventResponse() {}
    
    public TrackingEventResponse(String eventType, Date eventTime, String location, String voyageNumber) {
        this.eventType = eventType;
        this.eventTime = eventTime != null ? new Date(eventTime.getTime()) : null;
        this.location = location;
        this.voyageNumber = voyageNumber;
        this.description = generateDescription();
        this.isMilestone = determineIfMilestone(eventType);
    }
    
    // Factory method from domain value object
    public static TrackingEventResponse fromDomain(TrackingEvent domainEvent) {
        if (domainEvent == null) {
            return null;
        }
        
        return new TrackingEventResponse(
            domainEvent.getTrackingEventType().getEventType(),
            domainEvent.getTrackingEventType().getEventTime(),
            domainEvent.getTrackingLocation().getUnLocCode(),
            domainEvent.getTrackingVoyageNumber().getVoyageNumber()
        );
    }
    
    // Helper methods
    private String generateDescription() {
        if (eventType == null || location == null) {
            return "";
        }
        
        switch (eventType.toUpperCase()) {
            case "RECEIVED":
                return String.format("Cargo received at %s", location);
            case "LOADED":
                return String.format("Cargo loaded onto voyage %s at %s", 
                    voyageNumber != null ? voyageNumber : "unknown", location);
            case "UNLOADED":
                return String.format("Cargo unloaded from voyage %s at %s", 
                    voyageNumber != null ? voyageNumber : "unknown", location);
            case "CUSTOMS":
                return String.format("Customs clearance at %s", location);
            case "CLAIMED":
                return String.format("Cargo claimed by consignee at %s", location);
            default:
                return String.format("%s at %s", eventType, location);
        }
    }
    
    private boolean determineIfMilestone(String eventType) {
        if (eventType == null) {
            return false;
        }
        
        String upperType = eventType.toUpperCase();
        return upperType.equals("LOADED") || 
               upperType.equals("UNLOADED") || 
               upperType.equals("RECEIVED") || 
               upperType.equals("CLAIMED");
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String eventType;
        private Date eventTime;
        private String location;
        private String voyageNumber;
        
        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public Builder eventTime(Date eventTime) {
            this.eventTime = eventTime;
            return this;
        }
        
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        
        public Builder voyageNumber(String voyageNumber) {
            this.voyageNumber = voyageNumber;
            return this;
        }
        
        public TrackingEventResponse build() {
            return new TrackingEventResponse(eventType, eventTime, location, voyageNumber);
        }
    }
    
    // Getters e Setters
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
        this.description = generateDescription();
        this.isMilestone = determineIfMilestone(eventType);
    }
    
    public Date getEventTime() {
        return eventTime != null ? new Date(eventTime.getTime()) : null;
    }
    
    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime != null ? new Date(eventTime.getTime()) : null;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
        this.description = generateDescription();
    }
    
    public String getVoyageNumber() {
        return voyageNumber;
    }
    
    public void setVoyageNumber(String voyageNumber) {
        this.voyageNumber = voyageNumber;
        this.description = generateDescription();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isMilestone() {
        return isMilestone;
    }
    
    public void setMilestone(boolean milestone) {
        isMilestone = milestone;
    }
    
    @Override
    public String toString() {
        return "TrackingEventResponse{" +
                "eventType='" + eventType + '\'' +
                ", eventTime=" + eventTime +
                ", location='" + location + '\'' +
                ", voyageNumber='" + voyageNumber + '\'' +
                ", isMilestone=" + isMilestone +
                '}';
    }
}
