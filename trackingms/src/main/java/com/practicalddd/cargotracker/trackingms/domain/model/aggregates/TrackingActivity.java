package com.practicalddd.cargotracker.trackingms.domain.model.aggregates;

import com.practicalddd.cargotracker.trackingms.domain.model.commands.AddTrackingEventCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AssignTrackingNumberCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.events.TrackingActivityCreated;
import com.practicalddd.cargotracker.trackingms.domain.model.events.TrackingEventAdded;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrackingActivity {
    
    private Long id;  // ID para preservar entre camadas
    private TrackingNumber trackingNumber;
    private BookingId bookingId;
    private TrackingActivityEvents events;  // ← AGORA: Value Object Imutável
    private List<Object> domainEvents;

    // Construtor para nova atividade (sem ID)
    private TrackingActivity() {
        this.events = TrackingActivityEvents.EMPTY;
        this.domainEvents = new ArrayList<>();
    }

    // Construtor para nova atividade (sem ID) via command
    public TrackingActivity(AssignTrackingNumberCommand command) {
        this();
        this.trackingNumber = new TrackingNumber(command.getTrackingNumber());
        this.bookingId = new BookingId(command.getBookingId());
        
        registerEvent(new TrackingActivityCreated(
            this.trackingNumber.getTrackingNumber(),
            this.bookingId.getBookingId()
        ));
    }

    // Método de fábrica para reconstrução com ID
    public static TrackingActivity reconstruct(Long id, String trackingNumber, 
                                             String bookingId, List<TrackingEvent> events) {
        TrackingActivity activity = new TrackingActivity();
        activity.id = id;
        activity.trackingNumber = new TrackingNumber(trackingNumber);
        activity.bookingId = new BookingId(bookingId);
        
        if (events == null || events.isEmpty()) {
            activity.events = TrackingActivityEvents.EMPTY;
        } else {
            activity.events = new TrackingActivityEvents(events);
        }
        
        return activity;
    }

    // Método para adicionar evento (imutável - retorna novo agregado)
    public void addTrackingEvent(AddTrackingEventCommand command) {
        if (command == null) return;
        
        TrackingEvent trackingEvent = new TrackingEvent(
            new TrackingVoyageNumber(command.getVoyageNumber()),
            new TrackingLocation(command.getLocation()),
            new TrackingEventType(command.getEventType(), command.getEventTime())
        );
        
        // Cria nova instância de TrackingActivityEvents com o evento adicional
        this.events = this.events.addEvent(trackingEvent);
        
        registerEvent(new TrackingEventAdded(
            this.bookingId.getBookingId(),
            command.getEventType(),
            command.getLocation(),
            command.getEventTime()
        ));
    }

    // Método para adicionar evento diretamente (útil para reconstrução)
    private void addTrackingEvent(TrackingEvent event) {
        if (event != null) {
            this.events = this.events.addEvent(event);
        }
    }

    // Método para reconstruir eventos (compatibilidade)
    void reconstructEvents(List<TrackingEvent> events) {
        if (events == null) {
            this.events = TrackingActivityEvents.EMPTY;
        } else {
            this.events = new TrackingActivityEvents(events);
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public TrackingNumber getTrackingNumber() {
        return this.trackingNumber;
    }

    public BookingId getBookingId() {
        return this.bookingId;
    }

    // Retorna o Value Object completo
    public TrackingActivityEvents getEvents() {
        return this.events;
    }

    // Retorna apenas a lista de eventos (para compatibilidade)
    public List<TrackingEvent> getAllTrackingEvents() {
        return this.events != null 
            ? this.events.getEvents() 
            : Collections.emptyList();
    }

    public int getEventCount() {
        return this.events != null ? this.events.count() : 0;
    }

    public boolean hasEvents() {
        return this.events != null && !this.events.isEmpty();
    }

    public List<Object> getDomainEvents() {
        return new ArrayList<>(this.domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    private void registerEvent(Object event) {
        this.domainEvents.add(event);
    }

    // Para debug/logging
    @Override
    public String toString() {
        return "TrackingActivity{" +
                "id=" + id +
                ", trackingNumber=" + (trackingNumber != null ? trackingNumber.getTrackingNumber() : "null") +
                ", bookingId=" + (bookingId != null ? bookingId.getBookingId() : "null") +
                ", eventsCount=" + (events != null ? events.count() : 0) +
                '}';
    }
}
