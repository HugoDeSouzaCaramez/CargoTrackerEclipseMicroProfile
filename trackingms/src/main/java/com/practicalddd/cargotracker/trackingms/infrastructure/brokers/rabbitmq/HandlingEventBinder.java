package com.practicalddd.cargotracker.trackingms.infrastructure.brokers.rabbitmq;

import com.practicalddd.cargotracker.shareddomain.events.CargoHandledEvent;
import com.practicalddd.cargotracker.rabbitmqadaptor.EventBinder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HandlingEventBinder extends EventBinder {

    protected void bindEvents() {
        bind(CargoHandledEvent.class)
                .toQueue("cargotracker.handlingqueue")
                .autoAck();  // auto acknowledge
    }

    @PostConstruct
    public void initialize(){
        try{
            super.initialize();
            System.out.println("✅ HandlingEventBinder initialized successfully");
        }catch(Exception ex){
            System.err.println("❌ Error initializing HandlingEventBinder: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}