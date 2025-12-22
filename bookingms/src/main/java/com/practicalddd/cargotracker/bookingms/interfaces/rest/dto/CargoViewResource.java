package com.practicalddd.cargotracker.bookingms.interfaces.rest.dto;

import java.time.LocalDateTime;

/**
 * DTO otimizado para consultas de views materializadas.
 */
public class CargoViewResource {
    private String bookingId;
    private int bookingAmount;
    private String originLocation;
    private String destinationLocation;
    private LocalDateTime arrivalDeadline;
    private String status;
    private String routingStatus;
    private String transportStatus;
    private String lastKnownLocation;
    private String currentVoyage;
    private int legCount;
    private long estimatedTransitHours;
    private boolean isOnTrack;
    private boolean isMisdirected;
    private boolean isReadyForClaim;
    private boolean isUrgent;
    private boolean isDelayed;
    private LocalDateTime lastUpdated;
    
    // Construtores
    public CargoViewResource() {}
    
    public CargoViewResource(String bookingId, int bookingAmount, String originLocation,
                           String destinationLocation, LocalDateTime arrivalDeadline,
                           String status, String routingStatus, String transportStatus,
                           String lastKnownLocation, String currentVoyage, int legCount,
                           long estimatedTransitHours, boolean isOnTrack, boolean isMisdirected,
                           boolean isReadyForClaim, boolean isUrgent, boolean isDelayed,
                           LocalDateTime lastUpdated) {
        this.bookingId = bookingId;
        this.bookingAmount = bookingAmount;
        this.originLocation = originLocation;
        this.destinationLocation = destinationLocation;
        this.arrivalDeadline = arrivalDeadline;
        this.status = status;
        this.routingStatus = routingStatus;
        this.transportStatus = transportStatus;
        this.lastKnownLocation = lastKnownLocation;
        this.currentVoyage = currentVoyage;
        this.legCount = legCount;
        this.estimatedTransitHours = estimatedTransitHours;
        this.isOnTrack = isOnTrack;
        this.isMisdirected = isMisdirected;
        this.isReadyForClaim = isReadyForClaim;
        this.isUrgent = isUrgent;
        this.isDelayed = isDelayed;
        this.lastUpdated = lastUpdated;
    }
    
    // Getters e Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public int getBookingAmount() { return bookingAmount; }
    public void setBookingAmount(int bookingAmount) { this.bookingAmount = bookingAmount; }
    
    public String getOriginLocation() { return originLocation; }
    public void setOriginLocation(String originLocation) { this.originLocation = originLocation; }
    
    public String getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; }
    
    public LocalDateTime getArrivalDeadline() { return arrivalDeadline; }
    public void setArrivalDeadline(LocalDateTime arrivalDeadline) { this.arrivalDeadline = arrivalDeadline; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getRoutingStatus() { return routingStatus; }
    public void setRoutingStatus(String routingStatus) { this.routingStatus = routingStatus; }
    
    public String getTransportStatus() { return transportStatus; }
    public void setTransportStatus(String transportStatus) { this.transportStatus = transportStatus; }
    
    public String getLastKnownLocation() { return lastKnownLocation; }
    public void setLastKnownLocation(String lastKnownLocation) { this.lastKnownLocation = lastKnownLocation; }
    
    public String getCurrentVoyage() { return currentVoyage; }
    public void setCurrentVoyage(String currentVoyage) { this.currentVoyage = currentVoyage; }
    
    public int getLegCount() { return legCount; }
    public void setLegCount(int legCount) { this.legCount = legCount; }
    
    public long getEstimatedTransitHours() { return estimatedTransitHours; }
    public void setEstimatedTransitHours(long estimatedTransitHours) { this.estimatedTransitHours = estimatedTransitHours; }
    
    public boolean isOnTrack() { return isOnTrack; }
    public void setOnTrack(boolean onTrack) { isOnTrack = onTrack; }
    
    public boolean isMisdirected() { return isMisdirected; }
    public void setMisdirected(boolean misdirected) { isMisdirected = misdirected; }
    
    public boolean isReadyForClaim() { return isReadyForClaim; }
    public void setReadyForClaim(boolean readyForClaim) { isReadyForClaim = readyForClaim; }
    
    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }
    
    public boolean isDelayed() { return isDelayed; }
    public void setDelayed(boolean delayed) { isDelayed = delayed; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
