package com.practicalddd.cargotracker.bookingms;

import java.io.IOException;
import java.util.logging.LogManager;

import com.practicalddd.cargotracker.bookingms.infrastructure.brokers.rabbitmq.BookingEventBinder;
import com.practicalddd.cargotracker.bookingms.infrastructure.brokers.rabbitmq.RoutingEventBinder;
import io.helidon.microprofile.server.Server;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

public final class Main {

    private Main() { }

    public static void main(final String[] args) throws IOException {
        setupLogging();
        Server server = startServer();
        
        BeanManager beanManager = server.cdiContainer().getBeanManager();
        initializeEventBinders(beanManager);
        
        System.out.println("BookingMS started with EventBinders initialized");
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
            System.out.println("Initializing BookingEventBinder...");
            
            // Inicializar BookingEventBinder
            Bean<BookingEventBinder> bookingEventBinderBean = 
                (Bean<BookingEventBinder>) beanManager.resolve(
                    beanManager.getBeans(BookingEventBinder.class));
            BookingEventBinder bookingEventBinder = beanManager.getContext(
                bookingEventBinderBean.getScope()).get(bookingEventBinderBean, 
                beanManager.createCreationalContext(bookingEventBinderBean));

            // Inicializar RoutingEventBinder
            System.out.println("Initializing RoutingEventBinder...");
            Bean<RoutingEventBinder> routingEventBinderBean = 
                (Bean<RoutingEventBinder>) beanManager.resolve(
                    beanManager.getBeans(RoutingEventBinder.class));
            RoutingEventBinder routingEventBinder = beanManager.getContext(
                routingEventBinderBean.getScope()).get(routingEventBinderBean, 
                beanManager.createCreationalContext(routingEventBinderBean));
            
            System.out.println("All EventBinders initialized successfully");
                
        } catch (Exception e) {
            System.err.println("ERROR initializing EventBinders: " + e.getMessage());
            e.printStackTrace();
        }
    }
}