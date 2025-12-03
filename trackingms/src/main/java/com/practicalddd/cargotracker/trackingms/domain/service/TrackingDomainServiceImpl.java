package com.practicalddd.cargotracker.trackingms.domain.service;

import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingActivity;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AddTrackingEventCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AssignTrackingNumberCommand;

import javax.enterprise.context.ApplicationScoped;
import java.util.Date;

@ApplicationScoped
public class TrackingDomainServiceImpl implements TrackingDomainService {

    @Override
    public TrackingActivity createTrackingActivity(AssignTrackingNumberCommand command) {
        // Validação do comando
        if (command.getBookingId() == null || command.getBookingId().trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        
        if (command.getTrackingNumber() == null || command.getTrackingNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Tracking number is required");
        }
        
        return new TrackingActivity(command);
    }

    @Override
    public void addTrackingEventToActivity(TrackingActivity activity, AddTrackingEventCommand command) {
        // Validação prévia
        validateTrackingEvent(command);
        
        // Verificar se a atividade pertence ao booking correto
        if (!activity.getBookingId().getBookingId().equals(command.getBookingId())) {
            throw new IllegalArgumentException(
                "Event booking ID does not match activity booking ID"
            );
        }
        
        // Adicionar evento
        activity.addTrackingEvent(command);
    }

    @Override
    public boolean validateTrackingEvent(AddTrackingEventCommand command) {
        if (command.getBookingId() == null || command.getBookingId().trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        
        if (command.getEventType() == null || command.getEventType().trim().isEmpty()) {
            throw new IllegalArgumentException("Event type is required");
        }
        
        if (command.getEventTime() == null) {
            command.setEventTime(new Date()); // Usar data atual se não fornecida
        }
        
        if (command.getLocation() == null || command.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        
        return true;
    }
}
