package com.practicalddd.cargotracker.bookingms.application.ports.outbound;

/**
 * Porta de saída para serviços de auditoria.
 */
public interface AuditService {
    /**
     * Registra uma ação para auditoria
     */
    void logAction(String action, String entityType, String entityId, String userId, String details);
    
    /**
     * Registra uma tentativa de acesso não autorizado
     */
    void logSecurityEvent(String event, String userId, String resource, boolean successful);
    
    /**
     * Registra mudanças em entidades importantes
     */
    void logDataChange(String entityType, String entityId, String changeType, String oldValue, String newValue);
}
