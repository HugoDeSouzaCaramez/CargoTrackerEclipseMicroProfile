package com.practicalddd.cargotracker.bookingms.infrastructure.services;

import com.practicalddd.cargotracker.bookingms.application.ports.outbound.NotificationService;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

/**
 * Implementação mock do serviço de notificação.
 * No futuro, integrará com:
 * - Email (SMTP)
 * - SMS (Twilio, etc.)
 * - Push notifications
 * - Sistema de mensageria interno
 */
@ApplicationScoped
public class MockNotificationServiceImpl implements NotificationService {
    
    private static final Logger logger = Logger.getLogger(MockNotificationServiceImpl.class.getName());

    @Override
    public void notifyBookingCreated(String bookingId, String origin, String destination, String customerEmail) {
        logger.info(String.format(
            "[NOTIFICATION] Booking %s created: %s -> %s. Customer: %s",
            bookingId, origin, destination, customerEmail
        ));
        // No futuro: enviar email/SMS para o cliente
    }

    @Override
    public void notifyRouteAssigned(String bookingId, String customerEmail) {
        logger.info(String.format(
            "[NOTIFICATION] Route assigned for booking %s. Customer: %s",
            bookingId, customerEmail
        ));
        // No futuro: notificar sobre rota definida
    }

    @Override
    public void notifyCargoIssue(String bookingId, String issue, String customerEmail) {
        logger.warning(String.format(
            "[NOTIFICATION] Issue with booking %s: %s. Customer: %s",
            bookingId, issue, customerEmail
        ));
        // No futuro: alertar sobre problemas
    }
}
