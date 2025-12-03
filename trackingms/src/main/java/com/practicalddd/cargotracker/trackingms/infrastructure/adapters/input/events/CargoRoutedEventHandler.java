package com.practicalddd.cargotracker.trackingms.infrastructure.adapters.input.events;

import com.practicalddd.cargotracker.shareddomain.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.trackingms.application.internal.commandservices.AssignTrackingIdCommandService;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AssignTrackingNumberCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingNumber;
import com.practicalddd.cargotracker.trackingms.interfaces.events.transform.TrackingDetailsCommandEventAssembler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.logging.Logger;

@ApplicationScoped
public class CargoRoutedEventHandler {

    private static final Logger logger = Logger.getLogger(CargoRoutedEventHandler.class.getName());

    @Inject
    private AssignTrackingIdCommandService assignTrackingIdCommandService;

    @Transactional
    public void observeCargoRoutedEvent(@Observes CargoRoutedEvent event) {
        logger.info("🎯 === CARGO ROUTED EVENT RECEIVED ===");
        logger.info("📦 Booking ID: " + event.getContent().getBookingId());
        
        try {
            AssignTrackingNumberCommand command = TrackingDetailsCommandEventAssembler
                    .toCommandFromEvent(event);
            
            TrackingNumber trackingNumber = assignTrackingIdCommandService
                    .assignTrackingNumberToCargo(command);
            
            logger.info("✅ Tracking number assigned: " + trackingNumber.getTrackingNumber() + 
                       " for booking: " + event.getContent().getBookingId());
            
        } catch (Exception e) {
            logger.severe("❌ Error assigning tracking number: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw para rollback transacional
        }
    }
}
