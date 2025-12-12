package com.practicalddd.cargotracker.bookingms.application.internal.validators;

import com.practicalddd.cargotracker.bookingms.infrastructure.config.AppConfig;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Serviço centralizado para validações de domínio e aplicação.
 * Remove duplicação e padroniza mensagens de erro.
 */
@ApplicationScoped
public class CargoValidator {

    @Inject
    private AppConfig appConfig;

    // Validações de portos suportados
    public void validateSupportedPorts(String origin, String destination) {
        Set<String> supportedPorts = new HashSet<>(Arrays.asList(appConfig.getSupportedPorts()));
        String originUpper = origin.toUpperCase();
        String destUpper = destination.toUpperCase();
        
        if (!supportedPorts.contains(originUpper)) {
            throw new IllegalArgumentException(
                String.format("Origin port '%s' is not supported. Supported ports: %s", 
                    origin, String.join(", ", supportedPorts))
            );
        }
        
        if (!supportedPorts.contains(destUpper)) {
            throw new IllegalArgumentException(
                String.format("Destination port '%s' is not supported. Supported ports: %s", 
                    destination, String.join(", ", supportedPorts))
            );
        }
    }

    // Validação de prazo de chegada (consistente em todas as camadas)
    public void validateArrivalDeadline(LocalDateTime deadline) {
        LocalDateTime minDeadline = LocalDateTime.now().plusHours(appConfig.getMinDeadlineHours());
        
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Arrival deadline cannot be in the past");
        }
        
        if (deadline.isBefore(minDeadline)) {
            throw new IllegalArgumentException(
                String.format("Arrival deadline must be at least %d hours from now. Minimum required: %s", 
                    appConfig.getMinDeadlineHours(), minDeadline)
            );
        }
    }

    // Validação de quantidade de booking
    public void validateBookingAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Booking amount must be positive");
        }
        
        if (amount > appConfig.getMaxBookingAmount()) {
            throw new IllegalArgumentException(
                String.format("Booking amount cannot exceed %d", appConfig.getMaxBookingAmount())
            );
        }
    }

    // Validação cross-aggregate: verificar duplicidade de booking
    public void validateNoDuplicateBooking(String origin, String destination, LocalDateTime deadline) {
        // TODO: Implementar verificação real no repositório
        System.out.println("[CROSS-AGGREGATE VALIDATION] Checking for duplicate bookings: " + 
            origin + " -> " + destination + " around " + deadline);
    }

    // Validação cross-aggregate: capacidade do porto
    public void validateCapacityAvailability(String portCode, LocalDateTime start, LocalDateTime end, int amount) {
        // TODO: Implementar verificação de capacidade com serviço externo
        System.out.println("[CROSS-AGGREGATE VALIDATION] Checking port capacity for " + 
            portCode + " from " + start + " to " + end + " for amount " + amount);
    }

    // Validação cross-aggregate: restrições de porto
    public void validatePortRestrictions(String portCode, int amount, String cargoType) {
        // TODO: Implementar verificação de restrições de porto
        System.out.println("[CROSS-AGGREGATE VALIDATION] Checking port restrictions for " + 
            portCode + " with amount " + amount + " and type " + cargoType);
    }

    // Validação cross-aggregate: conflitos com outras operações
    public void validateCrossAggregateConstraints(String origin, String destination, LocalDateTime deadline, int amount) {
        // Validações complexas que envolvem múltiplos agregados
        // Exemplo: verificar se há operações conflitantes no mesmo período
        // Exemplo: verificar disponibilidade de recursos compartilhados
        
        // Por enquanto, apenas log
        System.out.println("[CROSS-AGGREGATE VALIDATION] Validating cross-aggregate constraints for " + 
            origin + " -> " + destination + " at " + deadline + " with amount " + amount);
    }
}
