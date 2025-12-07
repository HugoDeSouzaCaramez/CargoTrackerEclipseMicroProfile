package com.practicalddd.cargotracker.bookingms.infrastructure.brokers.rabbitmq;

import com.practicalddd.cargotracker.bookingms.application.ports.outbound.events.CargoRoutedIntegrationEvent;
import com.practicalddd.cargotracker.rabbitmqadaptor.EventBinder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RoutingEventBinder extends EventBinder {

    protected void bindEvents(){
        bind(CargoRoutedIntegrationEvent.class).toExchange("cargotracker.cargoroutings")
                .withRoutingKey("cargoroutings")
                .withPersistentMessages()
                .withPublisherConfirms();
    }

    @PostConstruct
    public void initialize(){
        try{
            super.initialize();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
