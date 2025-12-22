package com.practicalddd.cargotracker.bookingms.domain.services;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.DeadlinePolicy;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class CargoPriorityService {
    
    public boolean canExpedite(DeadlinePolicy policy) {
        // Regra de negócio: só pode expeditar se for urgente mas não crítico
        return policy.isUrgent() && !policy.isCritical() && policy.getRemainingHours() > 12;
    }
    
    public double calculateExpediteFee(DeadlinePolicy policy) {
        if (!policy.isUrgent()) {
            return 0.0;
        }
        
        long hoursRemaining = policy.getRemainingHours();
        // Usar a constante CRITICAL_THRESHOLD_HOURS
        if (hoursRemaining < DeadlinePolicy.CRITICAL_THRESHOLD_HOURS) {
            return 500.0; // Taxa alta para menos de 48h
        } else if (hoursRemaining < 96) { // 4 dias = 96 horas
            return 250.0; // Taxa média para 2-4 dias
        } else {
            return 100.0; // Taxa baixa para 4-7 dias
        }
    }
    
    public LocalDateTime calculateRecommendedBookingDeadline(LocalDateTime targetArrival) {
        // Este método agora precisa saber se é intercontinental
        // Vamos assumir o pior caso (intercontinental) para recomendar deadlines
        return calculateRecommendedBookingDeadline(targetArrival, true);
    }
    
    public LocalDateTime calculateRecommendedBookingDeadline(LocalDateTime targetArrival, boolean isIntercontinental) {
        // Recomenda deadline baseado na categoria
        DeadlinePolicy policy = new DeadlinePolicy(targetArrival, isIntercontinental);
        
        switch (policy.getPriorityCategory()) {
            case "CRITICAL":
                return targetArrival.plusHours(2); // Buffer mínimo
            case "URGENT":
                if (isIntercontinental) {
                    return targetArrival.plusDays(2);  // Buffer de 2 dias para rotas intercontinentais urgentes
                } else {
                    return targetArrival.plusDays(1);  // Buffer de 1 dia para rotas regionais urgentes
                }
            default:
                if (isIntercontinental) {
                    return targetArrival.plusDays(4);  // Buffer de 4 dias para rotas intercontinentais padrão
                } else {
                    return targetArrival.plusDays(3);  // Buffer de 3 dias para rotas regionais padrão
                }
        }
    }
    
    // Método para calcular fee considerando se é intercontinental
    public double calculateExpediteFee(LocalDateTime deadline, boolean isIntercontinental) {
        DeadlinePolicy policy = new DeadlinePolicy(deadline, isIntercontinental);
        return calculateExpediteFee(policy);
    }
    
    // Método para verificar se pode expeditar
    public boolean canExpedite(LocalDateTime deadline, boolean isIntercontinental) {
        DeadlinePolicy policy = new DeadlinePolicy(deadline, isIntercontinental);
        return canExpedite(policy);
    }
}
