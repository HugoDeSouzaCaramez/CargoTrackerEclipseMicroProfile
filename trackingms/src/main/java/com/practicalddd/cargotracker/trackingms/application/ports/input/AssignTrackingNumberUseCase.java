package com.practicalddd.cargotracker.trackingms.application.ports.input;

import com.practicalddd.cargotracker.trackingms.application.dto.TrackingActivityResponse;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AssignTrackingNumberCommand;

public interface AssignTrackingNumberUseCase {
    
    TrackingActivityResponse assignTrackingNumber(AssignTrackingNumberCommand command);
    
    TrackingActivityResponse getTrackingActivity(String bookingId);
    
    TrackingActivityResponse getTrackingActivityByTrackingNumber(String trackingNumber);
}
