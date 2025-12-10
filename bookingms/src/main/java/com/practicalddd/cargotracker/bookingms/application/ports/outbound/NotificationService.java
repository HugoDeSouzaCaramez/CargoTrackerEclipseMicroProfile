package com.practicalddd.cargotracker.bookingms.application.ports.outbound;

/**
 * Porta de saída para serviços de notificação.
 * Define o contrato que os adaptadores de notificação devem implementar.
 */
public interface NotificationService {
    /**
     * Envia notificação sobre um booking criado
     */
    void notifyBookingCreated(String bookingId, String origin, String destination, String customerEmail);
    
    /**
     * Envia notificação sobre mudança de rota
     */
    void notifyRouteAssigned(String bookingId, String customerEmail);
    
    /**
     * Envia notificação sobre problemas com a carga
     */
    void notifyCargoIssue(String bookingId, String issue, String customerEmail);
}
