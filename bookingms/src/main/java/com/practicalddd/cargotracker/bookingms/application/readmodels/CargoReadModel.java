package com.practicalddd.cargotracker.bookingms.application.readmodels;

import java.time.LocalDateTime;

/**
 * Read Model otimizado para consultas de Cargo
 * Contém apenas dados necessários para leitura, sem lógica de negócio
 */
public class CargoReadModel {
    private String bookingId;
    private String origin;
    private String destination;
    private String status;
    private int bookingAmount;
    private LocalDateTime arrivalDeadline;
    private boolean isUrgent;
    private int legCount;
    private LocalDateTime createdAt;
    
    // Construtor, getters e setters
    public CargoReadModel() {}
    
    public CargoReadModel(String bookingId, String origin, String destination, 
                         String status, int bookingAmount, LocalDateTime arrivalDeadline,
                         boolean isUrgent, int legCount, LocalDateTime createdAt) {
        this.bookingId = bookingId;
        this.origin = origin;
        this.destination = destination;
        this.status = status;
        this.bookingAmount = bookingAmount;
        this.arrivalDeadline = arrivalDeadline;
        this.isUrgent = isUrgent;
        this.legCount = legCount;
        this.createdAt = createdAt;
    }
    
    // Getters e Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBookingAmount() {
        return bookingAmount;
    }

    public void setBookingAmount(int bookingAmount) {
        this.bookingAmount = bookingAmount;
    }

    public LocalDateTime getArrivalDeadline() {
        return arrivalDeadline;
    }

    public void setArrivalDeadline(LocalDateTime arrivalDeadline) {
        this.arrivalDeadline = arrivalDeadline;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean isUrgent) {
        this.isUrgent = isUrgent;
    }

    public int getLegCount() {
        return legCount;
    }

    public void setLegCount(int legCount) {
        this.legCount = legCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
