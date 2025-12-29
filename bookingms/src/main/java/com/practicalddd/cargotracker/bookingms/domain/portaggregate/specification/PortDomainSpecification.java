package com.practicalddd.cargotracker.bookingms.domain.portaggregate.specification;

import com.practicalddd.cargotracker.bookingms.domain.portaggregate.Port;
import com.practicalddd.cargotracker.bookingms.domain.specification.InMemorySpecification;
import com.practicalddd.cargotracker.bookingms.domain.specification.Specification;

/**
 * Specifications para o agregado de domínio Port
 * Usa métodos do agregado Port para avaliação em memória
 */
public class PortDomainSpecification {
    
    public static Specification<Port> congestedPorts() {
        return new InMemorySpecification<Port>() {
            @Override
            public boolean isSatisfiedBy(Port port) {
                return port.getCapacity().getUsagePercentage() > 80;
            }
        };
    }
    
    public static Specification<Port> byCountry(String country) {
        return new InMemorySpecification<Port>() {
            @Override
            public boolean isSatisfiedBy(Port port) {
                return port.getLocation().getCountry().equalsIgnoreCase(country);
            }
        };
    }
    
    public static Specification<Port> operationalPorts() {
        return new InMemorySpecification<Port>() {
            @Override
            public boolean isSatisfiedBy(Port port) {
                return port.getStatus().toString().equals("OPERATIONAL");
            }
        };
    }
    
    public static Specification<Port> withAvailableCapacity(int requiredCapacity) {
        return new InMemorySpecification<Port>() {
            @Override
            public boolean isSatisfiedBy(Port port) {
                return port.getCapacity().getAvailableCapacity() >= requiredCapacity;
            }
        };
    }
}
