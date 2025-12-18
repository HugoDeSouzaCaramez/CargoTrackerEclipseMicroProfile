package com.practicalddd.cargotracker.bookingms.infrastructure.services;

import com.practicalddd.cargotracker.bookingms.application.ports.outbound.BillingService;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.Location;

import javax.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@ApplicationScoped
public class MockBillingServiceImpl implements BillingService {
    
    private static final Logger logger = Logger.getLogger(MockBillingServiceImpl.class.getName());
    private static final BigDecimal BASE_RATE = new BigDecimal("100.00");
    private static final BigDecimal PER_KG_RATE = new BigDecimal("2.50");
    private static final BigDecimal URGENT_SURCHARGE = new BigDecimal("200.00");

    @Override
    public BigDecimal calculateFee(Location origin, Location destination, 
                                   int weight, LocalDateTime deadline, boolean isUrgent) {
        // Simulação simples: base + peso + urgência
        BigDecimal weightCost = new BigDecimal(weight).multiply(PER_KG_RATE);
        BigDecimal total = BASE_RATE.add(weightCost);
        
        if (isUrgent) {
            total = total.add(URGENT_SURCHARGE);
        }
        
        logger.info(String.format(
            "Calculated fee: %s for %skg from %s to %s (urgent: %s)",
            total, weight, origin.getUnLocCode(), destination.getUnLocCode(), isUrgent
        ));
        
        return total;
    }

    @Override
    public String generateInvoice(String bookingId, BigDecimal amount, String customerId) {
        String invoiceNumber = "INV-" + bookingId + "-" + System.currentTimeMillis();
        logger.info(String.format(
            "Generated invoice %s: $%s for customer %s",
            invoiceNumber, amount, customerId
        ));
        return invoiceNumber;
    }

    @Override
    public boolean validateCredit(String customerId, BigDecimal amount) {
        // Mock: sempre retorna true para clientes conhecidos
        logger.info(String.format(
            "Credit validation for customer %s: amount $%s - APPROVED",
            customerId, amount
        ));
        return true;
    }
}
