package com.practicalddd.cargotracker.trackingms.interfaces.events.transform;

import com.practicalddd.cargotracker.shareddomain.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AssignTrackingNumberCommand;

public class TrackingDetailsCommandEventAssembler {
    public static AssignTrackingNumberCommand toCommandFromEvent(CargoRoutedEvent cargoRoutedEvent) {
        System.out.println("🎯 Converting CargoRoutedEvent to AssignTrackingNumberCommand");
        System.out.println("📦 Booking ID from event: " + cargoRoutedEvent.getContent().getBookingId());
        
        return new AssignTrackingNumberCommand(
                cargoRoutedEvent.getContent().getBookingId(), 
                "");
    }
}
