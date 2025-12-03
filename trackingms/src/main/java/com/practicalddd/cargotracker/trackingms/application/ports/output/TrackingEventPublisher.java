package com.practicalddd.cargotracker.trackingms.application.ports.output;

import com.practicalddd.cargotracker.trackingms.domain.model.events.TrackingActivityCreated;
import com.practicalddd.cargotracker.trackingms.domain.model.events.TrackingEventAdded;

public interface TrackingEventPublisher {
    
    void publishTrackingActivityCreated(TrackingActivityCreated event);
    
    void publishTrackingEventAdded(TrackingEventAdded event);
    
    void publishDomainEvents(Object aggregate);
}
