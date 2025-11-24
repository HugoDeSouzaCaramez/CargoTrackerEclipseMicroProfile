package com.practicalddd.cargotracker.handlingms;

import java.io.IOException;
import java.util.logging.LogManager;

import com.practicalddd.cargotracker.handlingms.infrastructure.brokers.rabbitmq.HandlingEventBinder;
import io.helidon.microprofile.server.Server;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

public final class Main {

    private Main() { }

    public static void main(final String[] args) throws IOException {
        setupLogging();
        Server server = startServer();
        
        // Inicializar EventBinders - CRÍTICO para comunicação de eventos
        BeanManager beanManager = server.cdiContainer().getBeanManager();
        initializeEventBinders(beanManager);
        
        System.out.println("HandlingMS started with EventBinders initialized");
    }

    static Server startServer() {
        return Server.create().start();
    }

    private static void setupLogging() throws IOException {
        LogManager.getLogManager().readConfiguration(
                Main.class.getResourceAsStream("/logging.properties"));
    }

    private static void initializeEventBinders(BeanManager beanManager) {
        try {
            System.out.println("Initializing HandlingEventBinder...");
            
            // Inicializar HandlingEventBinder
            Bean<HandlingEventBinder> handlingEventBinderBean = 
                (Bean<HandlingEventBinder>) beanManager.resolve(
                    beanManager.getBeans(HandlingEventBinder.class));
            HandlingEventBinder handlingEventBinder = beanManager.getContext(
                handlingEventBinderBean.getScope()).get(handlingEventBinderBean, 
                beanManager.createCreationalContext(handlingEventBinderBean));
            
            System.out.println("HandlingEventBinder initialized successfully");
                
        } catch (Exception e) {
            System.err.println("ERROR initializing HandlingEventBinder: " + e.getMessage());
            e.printStackTrace();
        }
    }
}