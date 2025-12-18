package com.practicalddd.cargotracker.bookingms.application.events;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredOn();
    String eventType();
}
