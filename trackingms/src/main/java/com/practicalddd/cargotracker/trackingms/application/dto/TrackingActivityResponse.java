package com.practicalddd.cargotracker.trackingms.application.dto;

import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingActivity;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingEvent;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbProperty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TrackingActivityResponse {
    
    @JsonbProperty("trackingNumber")
    private String trackingNumber;
    
    @JsonbProperty("bookingId")
    private String bookingId;
    
    @JsonbProperty("events")
    private List<TrackingEventResponse> events;
    
    @JsonbProperty("lastEvent")
    private TrackingEventResponse lastEvent;
    
    @JsonbProperty("totalEvents")
    private int totalEvents;
    
    @JsonbProperty("createdAt")
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createdAt;
    
    @JsonbProperty("updatedAt")
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date updatedAt;
    
    @JsonbProperty("status")
    private String status;
    
    // Construtores
    public TrackingActivityResponse() {
        this.events = new ArrayList<>();
        this.createdAt = new Date();
    }
    
    public TrackingActivityResponse(String trackingNumber, String bookingId) {
        this();
        this.trackingNumber = trackingNumber;
        this.bookingId = bookingId;
    }
    
    public TrackingActivityResponse(String trackingNumber, String bookingId, 
                                   List<TrackingEventResponse> events) {
        this(trackingNumber, bookingId);
        this.events = events != null ? events : new ArrayList<>();
        this.totalEvents = this.events.size();
        if (!this.events.isEmpty()) {
            this.lastEvent = this.events.get(this.events.size() - 1);
        }
        updateStatus();
    }
    
    // Factory method from domain aggregate
    public static TrackingActivityResponse fromDomain(TrackingActivity trackingActivity) {
        if (trackingActivity == null) {
            return null;
        }
        
        TrackingActivityResponse response = new TrackingActivityResponse(
            trackingActivity.getTrackingNumber().getTrackingNumber(),
            trackingActivity.getBookingId().getBookingId()
        );
        
        // Converter eventos de domínio para DTOs
        List<TrackingEventResponse> eventResponses = trackingActivity.getAllTrackingEvents().stream()
            .map(TrackingEventResponse::fromDomain)
            .collect(Collectors.toList());
        
        response.setEvents(eventResponses);
        response.updateStatus();
        
        return response;
    }
    
    // Factory method from domain events
    public static TrackingActivityResponse fromDomain(String trackingNumber, String bookingId, 
                                                     List<TrackingEvent> domainEvents) {
        TrackingActivityResponse response = new TrackingActivityResponse(
            trackingNumber, bookingId
        );
        
        List<TrackingEventResponse> eventResponses = domainEvents.stream()
            .map(TrackingEventResponse::fromDomain)
            .collect(Collectors.toList());
        
        response.setEvents(eventResponses);
        response.updateStatus();
        
        return response;
    }
    
    // Helper method to update derived fields
    private void updateStatus() {
        if (events == null || events.isEmpty()) {
            this.status = "CREATED";
            return;
        }
        
        // Determinar status com base no último evento
        TrackingEventResponse lastEvent = events.get(events.size() - 1);
        String lastEventType = lastEvent.getEventType();
        
        switch (lastEventType.toUpperCase()) {
            case "LOADED":
                this.status = "IN_TRANSIT";
                break;
            case "UNLOADED":
                this.status = "ARRIVED";
                break;
            case "CUSTOMS":
                this.status = "IN_CUSTOMS";
                break;
            case "RECEIVED":
                this.status = "DELIVERED";
                break;
            case "CLAIMED":
                this.status = "COMPLETED";
                break;
            default:
                this.status = "IN_PROGRESS";
        }
    }
    
    // Builder pattern for fluent API
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String trackingNumber;
        private String bookingId;
        private List<TrackingEventResponse> events = new ArrayList<>();
        private Date createdAt;
        private Date updatedAt;
        
        public Builder trackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
            return this;
        }
        
        public Builder bookingId(String bookingId) {
            this.bookingId = bookingId;
            return this;
        }
        
        public Builder event(TrackingEventResponse event) {
            this.events.add(event);
            return this;
        }
        
        public Builder events(List<TrackingEventResponse> events) {
            this.events = events;
            return this;
        }
        
        public Builder createdAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public TrackingActivityResponse build() {
            TrackingActivityResponse response = new TrackingActivityResponse(
                trackingNumber, bookingId, events
            );
            
            if (createdAt != null) {
                response.setCreatedAt(createdAt);
            }
            
            if (updatedAt != null) {
                response.setUpdatedAt(updatedAt);
            }
            
            return response;
        }
    }
    
    // Getters e Setters
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public String getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
    
    public List<TrackingEventResponse> getEvents() {
        return events != null ? new ArrayList<>(events) : new ArrayList<>();
    }
    
    public void setEvents(List<TrackingEventResponse> events) {
        this.events = events != null ? new ArrayList<>(events) : new ArrayList<>();
        this.totalEvents = this.events.size();
        if (!this.events.isEmpty()) {
            this.lastEvent = this.events.get(this.events.size() - 1);
        }
        updateStatus();
    }
    
    public TrackingEventResponse getLastEvent() {
        return lastEvent;
    }
    
    public void setLastEvent(TrackingEventResponse lastEvent) {
        this.lastEvent = lastEvent;
    }
    
    public int getTotalEvents() {
        return totalEvents;
    }
    
    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }
    
    public Date getCreatedAt() {
        return createdAt != null ? new Date(createdAt.getTime()) : null;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt != null ? new Date(createdAt.getTime()) : null;
    }
    
    public Date getUpdatedAt() {
        return updatedAt != null ? new Date(updatedAt.getTime()) : null;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt != null ? new Date(updatedAt.getTime()) : null;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    // Métodos utilitários
    public boolean hasEvents() {
        return events != null && !events.isEmpty();
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status) || "DELIVERED".equals(status);
    }
    
    public boolean isInTransit() {
        return "IN_TRANSIT".equals(status);
    }
    
    @Override
    public String toString() {
        return "TrackingActivityResponse{" +
                "trackingNumber='" + trackingNumber + '\'' +
                ", bookingId='" + bookingId + '\'' +
                ", totalEvents=" + totalEvents +
                ", status='" + status + '\'' +
                '}';
    }
}
