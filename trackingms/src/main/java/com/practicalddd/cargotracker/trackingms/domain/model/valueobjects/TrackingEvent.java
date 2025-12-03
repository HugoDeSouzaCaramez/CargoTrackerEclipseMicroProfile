package com.practicalddd.cargotracker.trackingms.domain.model.valueobjects;

import java.util.Objects;

public class TrackingEvent {
    
    private final TrackingVoyageNumber trackingVoyageNumber;
    private final TrackingLocation trackingLocation;
    private final TrackingEventType trackingEventType;

    public TrackingEvent(
            TrackingVoyageNumber trackingVoyageNumber,
            TrackingLocation trackingLocation,
            TrackingEventType trackingEventType) {
        this.trackingVoyageNumber = trackingVoyageNumber;
        this.trackingLocation = trackingLocation;
        this.trackingEventType = trackingEventType;
    }

    public TrackingVoyageNumber getTrackingVoyageNumber() {
        return trackingVoyageNumber;
    }

    public TrackingLocation getTrackingLocation() {
        return trackingLocation;
    }

    public TrackingEventType getTrackingEventType() {
        return trackingEventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackingEvent that = (TrackingEvent) o;
        return Objects.equals(trackingVoyageNumber, that.trackingVoyageNumber) &&
               Objects.equals(trackingLocation, that.trackingLocation) &&
               Objects.equals(trackingEventType, that.trackingEventType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingVoyageNumber, trackingLocation, trackingEventType);
    }
}
