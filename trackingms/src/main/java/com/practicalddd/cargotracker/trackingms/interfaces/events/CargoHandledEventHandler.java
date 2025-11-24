package com.practicalddd.cargotracker.trackingms.interfaces.events;

import com.practicalddd.cargotracker.shareddomain.events.CargoHandledEvent;
import com.practicalddd.cargotracker.shareddomain.events.CargoHandledEventData;
import com.practicalddd.cargotracker.trackingms.application.internal.commandservices.AssignTrackingIdCommandService;
import com.practicalddd.cargotracker.trackingms.interfaces.events.transform.TrackingActivityCommandEventAssembler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.logging.Logger;

@ApplicationScoped
public class CargoHandledEventHandler {

    private static final Logger logger = Logger.getLogger(CargoHandledEventHandler.class.getName());

    private AssignTrackingIdCommandService assignTrackingIdCommandService;

    @Inject
    public CargoHandledEventHandler(AssignTrackingIdCommandService assignTrackingIdCommandService){
        this.assignTrackingIdCommandService = assignTrackingIdCommandService;
    }

    @Transactional
    public void observeCargoHandledEvent(@Observes CargoHandledEvent event) {
        logger.info("=== CARGO HANDLED EVENT RECEIVED ===");
        
        CargoHandledEventData eventData = event.getContent();
        logger.info("Booking ID: " + eventData.getBookingId());
        logger.info("Handling Type: " + eventData.getHandlingType());
        logger.info("Location: " + eventData.getHandlingLocation());
        logger.info("Voyage: " + eventData.getVoyageNumber());
        logger.info("Completion Time: " + eventData.getHandlingCompletionTime());
        
        try {
            assignTrackingIdCommandService.addTrackingEvent(
                    TrackingActivityCommandEventAssembler.toCommandFromEvent(event));
            logger.info("=== TRACKING EVENT ADDED SUCCESSFULLY ===");
        } catch (Exception e) {
            logger.severe("=== ERROR ADDING TRACKING EVENT: " + e.getMessage());
            e.printStackTrace();
        }
    }
}