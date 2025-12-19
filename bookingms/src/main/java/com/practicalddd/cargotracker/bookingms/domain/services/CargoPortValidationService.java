package com.practicalddd.cargotracker.bookingms.domain.services;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.Location;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.RouteSpecification;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.Port;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.repositories.PortRepository;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortStatus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

/**
 * Domain Service para validações cruzadas entre agregados Cargo e Port.
 * Mantém a consistência entre os dois bounded contexts.
 */
@ApplicationScoped
public class CargoPortValidationService {

    @Inject
    private PortRepository portRepository;

    /**
     * Valida se os portos de origem e destino existem e estão operacionais
     */
    public ValidationResult validatePortsForBooking(String originUnLocCode, String destinationUnLocCode) {
        ValidationResult result = new ValidationResult();
        
        // Validar porto de origem - usando isPresent() do Java 8
        Optional<Port> originPort = portRepository.findByUnLocCode(originUnLocCode);
        if (!originPort.isPresent()) {
            result.addError("Origin port not found: " + originUnLocCode);
        } else if (originPort.get().getStatus() != PortStatus.OPERATIONAL) {
            result.addError("Origin port is not operational: " + originUnLocCode);
        } else {
            result.setOriginPort(originPort.get());
        }
        
        // Validar porto de destino - usando isPresent() do Java 8
        Optional<Port> destinationPort = portRepository.findByUnLocCode(destinationUnLocCode);
        if (!destinationPort.isPresent()) {
            result.addError("Destination port not found: " + destinationUnLocCode);
        } else if (destinationPort.get().getStatus() != PortStatus.OPERATIONAL) {
            result.addError("Destination port is not operational: " + destinationUnLocCode);
        } else {
            result.setDestinationPort(destinationPort.get());
        }
        
        return result;
    }

    /**
     * Valida se há capacidade disponível no porto para uma carga específica
     */
    public ValidationResult validatePortCapacity(String portUnLocCode, int cargoAmount) {
        ValidationResult result = new ValidationResult();
        
        Optional<Port> port = portRepository.findByUnLocCode(portUnLocCode);
        if (!port.isPresent()) {
            result.addError("Port not found: " + portUnLocCode);
            return result;
        }
        
        Port portEntity = port.get();
        
        if (portEntity.getStatus() == PortStatus.CONGESTED) {
            result.addWarning("Port is congested: " + portUnLocCode);
        }
        
        if (!portEntity.canAccommodate(cargoAmount)) {
            result.addError("Port does not have enough capacity: " + portUnLocCode + 
                           " (available: " + portEntity.getCapacity().getAvailableCapacity() + 
                           ", required: " + cargoAmount + ")");
        } else {
            result.setPort(portEntity);
        }
        
        return result;
    }

    /**
     * Valida uma rota completa considerando todos os portos intermediários
     */
    public ValidationResult validateRouteForCargo(RouteSpecification routeSpecification, 
                                                  Cargo cargo, int bookingAmount) {
        ValidationResult result = new ValidationResult();
        
        // Validar porto de origem
        ValidationResult originValidation = validatePortCapacity(
            routeSpecification.getOrigin().getUnLocCode(), 
            bookingAmount
        );
        result.merge(originValidation);
        
        // Validar porto de destino
        ValidationResult destinationValidation = validatePortsForBooking(
            routeSpecification.getOrigin().getUnLocCode(),
            routeSpecification.getDestination().getUnLocCode()
        );
        result.merge(destinationValidation);
        
        // Validar portos intermediários se houver itinerário
        if (cargo != null && !cargo.getItinerary().isEmpty()) {
            cargo.getItinerary().getLegs().forEach(leg -> {
                // Validar capacidade no porto de carga
                ValidationResult loadPortValidation = validatePortCapacity(
                    leg.getLoadLocation().getUnLocCode(),
                    bookingAmount
                );
                result.merge(loadPortValidation);
                
                // Validar capacidade no porto de descarga
                ValidationResult unloadPortValidation = validatePortCapacity(
                    leg.getUnloadLocation().getUnLocCode(),
                    bookingAmount
                );
                result.merge(unloadPortValidation);
            });
        }
        
        return result;
    }

    /**
     * Valida se um booking pode ser realizado considerando restrições dos portos
     */
    public ValidationResult validateBookingFeasibility(String originUnLocCode, 
                                                     String destinationUnLocCode,
                                                     int bookingAmount) {
        ValidationResult result = new ValidationResult();
        
        // Validar existência e status dos portos
        ValidationResult portValidation = validatePortsForBooking(originUnLocCode, destinationUnLocCode);
        result.merge(portValidation);
        
        // Se os portos foram encontrados, validar capacidade
        if (portValidation.getOriginPort() != null) {
            ValidationResult capacityValidation = validatePortCapacity(originUnLocCode, bookingAmount);
            result.merge(capacityValidation);
        }
        
        // Verificar se há restrições específicas (ex: portos fechados para manutenção)
        if (portValidation.getOriginPort() != null && 
            portValidation.getOriginPort().getStatus() == PortStatus.MAINTENANCE) {
            result.addWarning("Origin port is under maintenance: " + originUnLocCode);
        }
        
        if (portValidation.getDestinationPort() != null && 
            portValidation.getDestinationPort().getStatus() == PortStatus.MAINTENANCE) {
            result.addWarning("Destination port is under maintenance: " + destinationUnLocCode);
        }
        
        return result;
    }

    /**
     * Classe para encapsular resultados de validação
     */
    public static class ValidationResult {
        private boolean valid = true;
        private StringBuilder errors = new StringBuilder();
        private StringBuilder warnings = new StringBuilder();
        private Port originPort;
        private Port destinationPort;
        private Port port;

        public void addError(String error) {
            valid = false;
            errors.append(error).append("; ");
        }
        
        public void addWarning(String warning) {
            warnings.append(warning).append("; ");
        }
        
        public void merge(ValidationResult other) {
            if (!other.valid) {
                this.valid = false;
            }
            this.errors.append(other.errors);
            this.warnings.append(other.warnings);
            
            if (other.originPort != null) this.originPort = other.originPort;
            if (other.destinationPort != null) this.destinationPort = other.destinationPort;
            if (other.port != null) this.port = other.port;
        }
        
        public void throwIfInvalid() {
            if (!isValid()) {
                throw new IllegalArgumentException(getErrorSummary());
            }
        }
        
        public boolean hasWarnings() {
            return warnings.length() > 0;
        }
        
        public String getErrorSummary() {
            return errors.toString();
        }
        
        public String getWarningSummary() {
            return warnings.toString();
        }
        
        // Getters e Setters
        public boolean isValid() { return valid; }
        public Port getOriginPort() { return originPort; }
        public void setOriginPort(Port originPort) { this.originPort = originPort; }
        public Port getDestinationPort() { return destinationPort; }
        public void setDestinationPort(Port destinationPort) { this.destinationPort = destinationPort; }
        public Port getPort() { return port; }
        public void setPort(Port port) { this.port = port; }
    }
}
