package com.practicalddd.cargotracker.bookingms.infrastructure.services;

import com.practicalddd.cargotracker.bookingms.application.ports.outbound.AuditService;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@ApplicationScoped
public class MockAuditServiceImpl implements AuditService {
    
    private static final Logger logger = Logger.getLogger(MockAuditServiceImpl.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void logAction(String action, String entityType, String entityId, 
                         String userId, String details) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format(
            "[AUDIT][%s] %s: %s %s by user %s. Details: %s",
            timestamp, action, entityType, entityId, userId, details
        );
        
        // No futuro: enviar para sistema de log centralizado/SIEM
        logger.info(logMessage);
    }

    @Override
    public void logSecurityEvent(String event, String userId, String resource, boolean successful) {
        String status = successful ? "SUCCESS" : "FAILURE";
        logger.warning(String.format(
            "[SECURITY] %s access to %s by user %s: %s",
            event, resource, userId, status
        ));
    }

    @Override
    public void logDataChange(String entityType, String entityId, 
                             String changeType, String oldValue, String newValue) {
        logger.info(String.format(
            "[DATA CHANGE] %s %s: %s from '%s' to '%s'",
            changeType, entityType, entityId, oldValue, newValue
        ));
    }
}
