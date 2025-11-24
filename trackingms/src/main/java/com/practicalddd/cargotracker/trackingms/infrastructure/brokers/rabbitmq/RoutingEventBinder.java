package com.practicalddd.cargotracker.trackingms.infrastructure.brokers.rabbitmq;

import com.practicalddd.cargotracker.shareddomain.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.rabbitmqadaptor.EventBinder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RoutingEventBinder extends EventBinder {

    protected void bindEvents() {
        bind(CargoRoutedEvent.class)
                .toQueue("cargotracker.routingqueue")
                .autoAck();  // auto acknowledge
    }

    @PostConstruct
    public void initialize() {
        try {
            super.initialize();
            System.out.println("✅ RoutingEventBinder initialized successfully");
        } catch (Exception ex) {
            System.err.println("❌ Error initializing RoutingEventBinder: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}