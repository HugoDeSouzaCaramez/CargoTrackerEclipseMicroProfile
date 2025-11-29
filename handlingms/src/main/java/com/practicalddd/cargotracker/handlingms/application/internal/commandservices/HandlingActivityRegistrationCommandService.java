package com.practicalddd.cargotracker.handlingms.application.internal.commandservices;

import com.practicalddd.cargotracker.handlingms.domain.model.aggregates.HandlingActivity;
import com.practicalddd.cargotracker.handlingms.domain.model.commands.HandlingActivityRegistrationCommand;
import com.practicalddd.cargotracker.handlingms.domain.model.repositories.HandlingActivityRepository;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.CargoBookingId;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.Type;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.VoyageNumber;
import com.practicalddd.cargotracker.shareddomain.events.CargoHandledEvent;
import com.practicalddd.cargotracker.shareddomain.events.CargoHandledEventData;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class HandlingActivityRegistrationCommandService {

    @Inject
    private HandlingActivityRepository handlingActivityRepository;

    @Inject
    private Event<CargoHandledEvent> cargoHandledEventControl;

    @Transactional
    public void registerHandlingActivityService(HandlingActivityRegistrationCommand command) {
        try {
            HandlingActivity handlingActivity = createHandlingActivity(command);
            handlingActivityRepository.store(handlingActivity);
            
            publishCargoHandledEvent(command);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register handling activity: " + e.getMessage(), e);
        }
    }

    private HandlingActivity createHandlingActivity(HandlingActivityRegistrationCommand command) {
        CargoBookingId cargoBookingId = new CargoBookingId(command.getBookingId());
        Location location = new Location(command.getUnLocode());
        Type handlingType = Type.valueOf(command.getHandlingType());

        if (command.getVoyageNumber() != null && !command.getVoyageNumber().isEmpty()) {
            VoyageNumber voyageNumber = new VoyageNumber(command.getVoyageNumber());
            return new HandlingActivity(cargoBookingId, command.getCompletionTime(), 
                                     handlingType, location, voyageNumber);
        } else {
            return new HandlingActivity(cargoBookingId, command.getCompletionTime(), 
                                     handlingType, location);
        }
    }

    private void publishCargoHandledEvent(HandlingActivityRegistrationCommand command) {
        CargoHandledEvent cargoHandledEvent = new CargoHandledEvent();
        CargoHandledEventData eventData = new CargoHandledEventData();
        
        eventData.setBookingId(command.getBookingId());
        eventData.setHandlingCompletionTime(command.getCompletionTime());
        eventData.setHandlingLocation(command.getUnLocode());
        eventData.setHandlingType(command.getHandlingType());
        eventData.setVoyageNumber(command.getVoyageNumber());

        cargoHandledEvent.setContent(eventData);
        cargoHandledEventControl.fire(cargoHandledEvent);
    }
}
