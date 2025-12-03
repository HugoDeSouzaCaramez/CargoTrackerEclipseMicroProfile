package com.practicalddd.cargotracker.trackingms.infrastructure.adapters.output.messaging;

import com.practicalddd.cargotracker.trackingms.application.ports.output.TrackingEventPublisher;
import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingActivity;
import com.practicalddd.cargotracker.trackingms.domain.model.events.TrackingActivityCreated;
import com.practicalddd.cargotracker.trackingms.domain.model.events.TrackingEventAdded;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class TrackingEventPublisherImpl implements TrackingEventPublisher {

    private static final Logger logger = Logger.getLogger(TrackingEventPublisherImpl.class.getName());

    @Override
    public void publishTrackingActivityCreated(TrackingActivityCreated event) {
        logger.info("Publishing TrackingActivityCreated event: " + 
                   event.getTrackingNumber() + " for booking: " + event.getBookingId());
        
        // Em produção, publicar para RabbitMQ/Kafka
        // Por enquanto, apenas log
        System.out.println("📤 EVENT PUBLISHED: TrackingActivityCreated - " + 
                          event.getTrackingNumber());
    }

    @Override
    public void publishTrackingEventAdded(TrackingEventAdded event) {
        logger.info("Publishing TrackingEventAdded event for booking: " + 
                   event.getBookingId() + " - " + event.getEventType());
        
        // Em produção, publicar para RabbitMQ/Kafka
        System.out.println("📤 EVENT PUBLISHED: TrackingEventAdded - " + 
                          event.getBookingId() + " - " + event.getEventType());
    }

    @Override
    public void publishDomainEvents(Object aggregate) {
        if (aggregate instanceof TrackingActivity) {
            TrackingActivity activity = (TrackingActivity) aggregate;
            List<Object> domainEvents = activity.getDomainEvents();
            
            for (Object event : domainEvents) {
                if (event instanceof TrackingActivityCreated) {
                    publishTrackingActivityCreated((TrackingActivityCreated) event);
                } else if (event instanceof TrackingEventAdded) {
                    publishTrackingEventAdded((TrackingEventAdded) event);
                }
            }
            
            activity.clearDomainEvents();
        }
    }
}
