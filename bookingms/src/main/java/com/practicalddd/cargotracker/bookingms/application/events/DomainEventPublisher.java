package com.practicalddd.cargotracker.bookingms.application.events;


public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
