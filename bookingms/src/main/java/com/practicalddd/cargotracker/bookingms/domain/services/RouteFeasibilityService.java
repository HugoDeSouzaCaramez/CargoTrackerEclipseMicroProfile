package com.practicalddd.cargotracker.bookingms.domain.services;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.DeadlinePolicy;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.Location;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@ApplicationScoped
public class RouteFeasibilityService {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.forLanguageTag("pt-BR"));
    
    public ValidationResult validateRouteFeasibility(Location origin, Location destination, 
                                                    LocalDateTime deadline) {
        ValidationResult result = new ValidationResult();
        
        boolean isIntercontinental = isIntercontinental(origin, destination);
        DeadlinePolicy policy = new DeadlinePolicy(deadline, isIntercontinental);
        
        // Validação básica de prazo
        if (!policy.isValidForBooking()) {
            result.addError(String.format(
                "Prazo insuficiente para rota %s de %s para %s. " +
                "Mínimo requerido: %s. " +
                "Prazo solicitado: %s (%d dias restantes). " +
                "Sugestão: Agende para após %s.",
                isIntercontinental ? "intercontinental" : "regional",
                origin.getUnLocCode(),
                destination.getUnLocCode(),
                policy.getMinimumRequiredDays(),
                deadline.format(DATE_FORMATTER),
                policy.getRemainingDays(),
                LocalDateTime.now().plusDays(isIntercontinental ? 
                    DeadlinePolicy.MIN_DAYS_INTERCONTINENTAL : 
                    DeadlinePolicy.MIN_DAYS_REGIONAL).format(DATE_FORMATTER)
            ));
            return result;
        }
        
        // Classificar urgência
        String category = policy.getPriorityCategory();
        result.setPriorityCategory(category);
        result.setIntercontinental(isIntercontinental);
        
        if (policy.isUrgent()) {
            result.addWarning(String.format(
                "Rota %s marcada como %s. " +
                "Origem: %s, Destino: %s. " +
                "Tempo restante: %d dias. " +
                "Prazo final: %s.",
                isIntercontinental ? "intercontinental" : "regional",
                category,
                origin.getUnLocCode(),
                destination.getUnLocCode(),
                policy.getRemainingDays(),
                deadline.format(DATE_FORMATTER)
            ));
        }
        
        // Verificar viabilidade operacional
        if (isIntercontinental && policy.getRemainingDays() < 21) {
            result.addWarning("ATENÇÃO: Rota intercontinental com menos de 21 dias pode requerer expedição especial com custos adicionais.");
        }
        
        // Verificar se é doméstico (poderia ter prazos menores)
        if (!isIntercontinental && isDomestic(origin, destination)) {
            result.addInfo("Rota doméstica identificada. Prazos mais curtos podem ser negociados.");
        }
        
        result.setValid(true);
        return result;
    }
    
    private boolean isIntercontinental(Location origin, Location destination) {
        // Lógica simplificada: verificar se os códigos de país são diferentes
        String originCountry = origin.getUnLocCode().substring(0, 2);
        String destCountry = destination.getUnLocCode().substring(0, 2);
        
        return !originCountry.equals(destCountry);
    }
    
    private boolean isDomestic(Location origin, Location destination) {
        // Rota doméstica se mesmo país
        String originCountry = origin.getUnLocCode().substring(0, 2);
        String destCountry = destination.getUnLocCode().substring(0, 2);
        
        return originCountry.equals(destCountry);
    }
    
    public static class ValidationResult {
        private boolean valid = false;
        private String error;
        private String warning;
        private String info;
        private String priorityCategory;
        private boolean isIntercontinental;
        
        public void addError(String error) {
            this.error = error;
            this.valid = false;
        }
        
        public void addWarning(String warning) {
            this.warning = warning;
        }
        
        public void addInfo(String info) {
            this.info = info;
        }
        
        public void setPriorityCategory(String category) {
            this.priorityCategory = category;
        }
        
        public void setIntercontinental(boolean intercontinental) {
            this.isIntercontinental = intercontinental;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public boolean isValid() { return valid; }
        public String getError() { return error; }
        public String getWarning() { return warning; }
        public String getInfo() { return info; }
        public String getPriorityCategory() { return priorityCategory; }
        public boolean isIntercontinental() { return isIntercontinental; }
        
        public void throwIfInvalid() {
            if (!isValid()) {
                throw new IllegalArgumentException(getError());
            }
        }
        
        public boolean hasWarnings() {
            return warning != null && !warning.isEmpty();
        }
        
        public String getWarningSummary() {
            return warning != null ? warning : "";
        }
    }
}
