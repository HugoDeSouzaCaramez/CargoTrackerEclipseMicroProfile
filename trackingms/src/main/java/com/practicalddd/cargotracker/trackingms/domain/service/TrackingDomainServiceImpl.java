package com.practicalddd.cargotracker.trackingms.domain.service;

import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingActivity;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AddTrackingEventCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AssignTrackingNumberCommand;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

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
                    "Event booking ID does not match activity booking ID");
        }

        // Verificar se o evento não está no futuro
        if (command.getEventTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Event time cannot be in the future");
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

        // Corrigido: usar LocalDateTime.now() em vez de new LocalDateTime()
        if (command.getEventTime() == null) {
            command.setEventTime(LocalDateTime.now()); // Usar data atual se não fornecida
        }

        if (command.getLocation() == null || command.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }

        // Validação adicional: verificar se o tipo de evento é válido
        validateEventType(command.getEventType());

        return true;
    }

    /**
     * Valida se o tipo de evento é suportado
     */
    private void validateEventType(String eventType) {
        if (eventType == null) {
            return;
        }

        String normalizedType = eventType.trim().toUpperCase();

        // Lista COMPLETA de tipos de evento suportados
        switch (normalizedType) {
            // Recebimento
            case "RECEIVE":
            case "RECEIVED":
                // Carregamento
            case "LOAD":
            case "LOADED":
                // Descarregamento
            case "UNLOAD":
            case "UNLOADED":
                // Outros tipos
            case "CUSTOMS":
            case "CLAIMED":
            case "INSPECTION":
            case "DELIVERED":
                // Tipos adicionais que o HandlingMS pode enviar
            case "CLAIM":
            case "INSPECT":
            case "DELIVER":
                return; // Tipo válido
            default:
                throw new IllegalArgumentException(
                        "Invalid event type: " + eventType +
                                ". Supported types: RECEIVE, RECEIVED, LOAD, LOADED, UNLOAD, UNLOADED, CUSTOMS, CLAIMED, INSPECTION, DELIVERED, CLAIM, INSPECT, DELIVER");
        }
    }
}
