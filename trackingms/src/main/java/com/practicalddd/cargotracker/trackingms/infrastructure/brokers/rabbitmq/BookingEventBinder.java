package com.practicalddd.cargotracker.trackingms.infrastructure.brokers.rabbitmq;

import com.practicalddd.cargotracker.shareddomain.events.CargoBookedEvent;
import com.practicalddd.cargotracker.rabbitmqadaptor.EventBinder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BookingEventBinder extends EventBinder {

    protected void bindEvents() {
        bind(CargoBookedEvent.class)
                .toQueue("cargotracker.bookingsqueue")
                .autoAck();  // auto acknowledge
    }

    @PostConstruct
    public void initialize() {
        try {
            super.initialize();
            System.out.println("✅ BookingEventBinder initialized successfully");
        } catch (Exception ex) {
            System.err.println("❌ Error initializing BookingEventBinder: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}