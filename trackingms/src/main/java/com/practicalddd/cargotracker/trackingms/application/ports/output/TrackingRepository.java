package com.practicalddd.cargotracker.trackingms.application.ports.output;

import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingActivity;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingNumber;

import java.util.List;
import java.util.Optional;

public interface TrackingRepository {
    
    Optional<TrackingActivity> findByTrackingNumber(TrackingNumber trackingNumber);
    
    Optional<TrackingActivity> findByBookingId(BookingId bookingId);
    
    TrackingActivity save(TrackingActivity trackingActivity);
    
    String generateNextTrackingNumber();
    
    List<TrackingActivity> findAll();
    
    List<TrackingNumber> findAllTrackingNumbers();
}
