package com.practicalddd.cargotracker.trackingms.infrastructure.adapters.output.persistence.mapper;

import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingActivity;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.*;
import com.practicalddd.cargotracker.trackingms.infrastructure.adapters.output.persistence.jpa.TrackingActivityJpa;
import com.practicalddd.cargotracker.trackingms.infrastructure.adapters.output.persistence.jpa.TrackingEventJpa;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TrackingPersistenceMapper {

    @PersistenceContext(unitName = "trackingms")
    private EntityManager entityManager;

    public TrackingActivityJpa toJpa(TrackingActivity domain) {
        if (domain == null)
            return null;

        TrackingActivityJpa existingJpa = findExistingByBookingId(domain.getBookingId().getBookingId());
        TrackingActivityJpa jpa = (existingJpa != null) ? existingJpa : new TrackingActivityJpa();

        // Atualizar campos
        jpa.setTrackingNumber(domain.getTrackingNumber().getTrackingNumber());
        jpa.setBookingId(domain.getBookingId().getBookingId());

        // Limpar eventos existentes
        jpa.clearTrackingEvents();

        // Adicionar novos eventos
        if (domain.getEvents() != null && !domain.getEvents().isEmpty()) {
            for (TrackingEvent domainEvent : domain.getEvents().getEvents()) {
                TrackingEventJpa eventJpa = new TrackingEventJpa();
                eventJpa.setBookingId(domain.getBookingId().getBookingId());

                if (domainEvent.getTrackingVoyageNumber() != null) {
                    eventJpa.setVoyageNumber(domainEvent.getTrackingVoyageNumber().getVoyageNumber());
                }

                if (domainEvent.getTrackingLocation() != null) {
                    eventJpa.setLocation(domainEvent.getTrackingLocation().getUnLocCode());
                }

                if (domainEvent.getTrackingEventType() != null) {
                    eventJpa.setEventType(domainEvent.getTrackingEventType().getEventType());
                    eventJpa.setEventTime(domainEvent.getTrackingEventType().getEventTime());
                }

                jpa.addTrackingEvent(eventJpa);
            }
        }

        return jpa;
    }

    public TrackingActivity toDomain(TrackingActivityJpa jpa) {
        if (jpa == null)
            return null;

        // Converter eventos JPA para domínio
        List<TrackingEvent> domainEvents = new ArrayList<>();

        if (jpa.getTrackingEvents() != null) {
            for (TrackingEventJpa eventJpa : jpa.getTrackingEvents()) {
                TrackingEvent domainEvent = new TrackingEvent(
                        new TrackingVoyageNumber(eventJpa.getVoyageNumber()),
                        new TrackingLocation(eventJpa.getLocation()),
                        new TrackingEventType(eventJpa.getEventType(), eventJpa.getEventTime()));
                domainEvents.add(domainEvent);
            }
        }

        return TrackingActivity.reconstruct(
                jpa.getId(),
                jpa.getTrackingNumber(),
                jpa.getBookingId(),
                domainEvents);
    }

    private TrackingActivityJpa findExistingByBookingId(String bookingId) {
        try {
            return entityManager
                    .createNamedQuery("TrackingActivityJpa.findByBookingId", TrackingActivityJpa.class)
                    .setParameter("bookingId", bookingId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
