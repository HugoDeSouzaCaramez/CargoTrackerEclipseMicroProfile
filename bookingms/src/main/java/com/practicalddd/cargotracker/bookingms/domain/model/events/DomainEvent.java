package com.practicalddd.cargotracker.bookingms.domain.model.events;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredOn();
    String eventType();
}