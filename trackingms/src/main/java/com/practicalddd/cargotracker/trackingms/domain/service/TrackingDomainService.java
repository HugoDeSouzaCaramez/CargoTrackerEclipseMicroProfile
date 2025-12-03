package com.practicalddd.cargotracker.trackingms.domain.service;

import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingActivity;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AddTrackingEventCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AssignTrackingNumberCommand;

public interface TrackingDomainService {
    
    TrackingActivity createTrackingActivity(AssignTrackingNumberCommand command);
    
    void addTrackingEventToActivity(TrackingActivity activity, AddTrackingEventCommand command);
    
    boolean validateTrackingEvent(AddTrackingEventCommand command);
}
