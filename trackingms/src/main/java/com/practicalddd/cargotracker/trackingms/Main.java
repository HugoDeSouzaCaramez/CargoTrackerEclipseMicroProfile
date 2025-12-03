package com.practicalddd.cargotracker.trackingms;

import java.io.IOException;
import java.util.logging.LogManager;

import io.helidon.microprofile.server.Server;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import com.practicalddd.cargotracker.trackingms.infrastructure.brokers.rabbitmq.BookingEventBinder;
import com.practicalddd.cargotracker.trackingms.infrastructure.brokers.rabbitmq.HandlingEventBinder;
import com.practicalddd.cargotracker.trackingms.infrastructure.brokers.rabbitmq.RoutingEventBinder;

public final class Main {

    private Main() { }

    public static void main(final String[] args) throws IOException {
        setupLogging();
        Server server = startServer();
        
        // INICIALIZAR TODOS OS EVENT BINDERS - CRÍTICO
        BeanManager beanManager = server.cdiContainer().getBeanManager();
        initializeAllEventBinders(beanManager);
        
        System.out.println("🚀 TrackingMS started with ALL EventBinders initialized");
    }

    static Server startServer() {
        return Server.create().start();
    }

    private static void setupLogging() throws IOException {
        LogManager.getLogManager().readConfiguration(
                Main.class.getResourceAsStream("/logging.properties"));
    }

    private static void initializeAllEventBinders(BeanManager beanManager) {
        try {
            System.out.println("🔧 Initializing all TrackingMS EventBinders...");
            
            // Inicializar BookingEventBinder (para CargoBookedEvent)
            Bean<BookingEventBinder> bookingEventBinderBean = 
                (Bean<BookingEventBinder>) beanManager.resolve(
                    beanManager.getBeans(BookingEventBinder.class));
            BookingEventBinder bookingEventBinder = beanManager.getContext(
                bookingEventBinderBean.getScope()).get(bookingEventBinderBean, 
                beanManager.createCreationalContext(bookingEventBinderBean));

            // Inicializar RoutingEventBinder (para CargoRoutedEvent)  
            Bean<RoutingEventBinder> routingEventBinderBean = 
                (Bean<RoutingEventBinder>) beanManager.resolve(
                    beanManager.getBeans(RoutingEventBinder.class));
            RoutingEventBinder routingEventBinder = beanManager.getContext(
                routingEventBinderBean.getScope()).get(routingEventBinderBean, 
                beanManager.createCreationalContext(routingEventBinderBean));

            // Inicializar HandlingEventBinder (para CargoHandledEvent)
            Bean<HandlingEventBinder> handlingEventBinderBean = 
                (Bean<HandlingEventBinder>) beanManager.resolve(
                    beanManager.getBeans(HandlingEventBinder.class));
            HandlingEventBinder handlingEventBinder = beanManager.getContext(
                handlingEventBinderBean.getScope()).get(handlingEventBinderBean, 
                beanManager.createCreationalContext(handlingEventBinderBean));
            
            System.out.println("✅ All TrackingMS EventBinders initialized successfully");
                
        } catch (Exception e) {
            System.err.println("❌ ERROR initializing TrackingMS EventBinders: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
