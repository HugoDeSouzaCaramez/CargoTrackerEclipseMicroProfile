package com.practicalddd.cargotracker.trackingms.domain.model.valueobjects;

import java.util.Objects;

public class TrackingNumber {
    
    private final String trackingNumber;

    public TrackingNumber(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Tracking number cannot be null or empty");
        }
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingNumber() {
        return this.trackingNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackingNumber that = (TrackingNumber) o;
        return trackingNumber.equals(that.trackingNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingNumber);
    }

    @Override
    public String toString() {
        return trackingNumber;
    }
}
