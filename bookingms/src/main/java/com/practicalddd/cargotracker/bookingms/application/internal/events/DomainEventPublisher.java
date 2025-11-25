package com.practicalddd.cargotracker.bookingms.application.internal.events;

import com.practicalddd.cargotracker.bookingms.domain.model.events.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}