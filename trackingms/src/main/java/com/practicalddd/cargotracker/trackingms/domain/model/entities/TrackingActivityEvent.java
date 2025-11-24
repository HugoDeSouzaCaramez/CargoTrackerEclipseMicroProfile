package com.practicalddd.cargotracker.trackingms.domain.model.entities;


import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingEvent;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class TrackingActivityEvent {
    public static final TrackingActivityEvent EMPTY_ACTIVITY = new TrackingActivityEvent(new ArrayList<>());
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tracking_activity_id") // Nome da coluna de junção
    private List<TrackingEvent> trackingEvents;

    public TrackingActivityEvent() {
        this.trackingEvents = new ArrayList<>();
    }

    public TrackingActivityEvent(List<TrackingEvent> trackingEvents) {
        this.trackingEvents = trackingEvents;
    }

    public List<TrackingEvent> getTrackingEvents() {
        return trackingEvents;
    }
}
