package com.practicalddd.cargotracker.bookingms.application.ports.outbound;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.Location;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Porta de saída para serviços de cobrança/tarifas.
 */
public interface BillingService {
    /**
     * Calcula a tarifa para um booking
     */
    BigDecimal calculateFee(
        Location origin, 
        Location destination, 
        int weight, 
        LocalDateTime deadline,
        boolean isUrgent
    );
    
    /**
     * Gera uma fatura para um booking
     */
    String generateInvoice(String bookingId, BigDecimal amount, String customerId);
    
    /**
     * Valida se um cliente tem crédito suficiente
     */
    boolean validateCredit(String customerId, BigDecimal amount);
}
