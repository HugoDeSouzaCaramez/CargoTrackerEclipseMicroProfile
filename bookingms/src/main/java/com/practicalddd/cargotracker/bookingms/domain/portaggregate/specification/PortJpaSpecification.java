package com.practicalddd.cargotracker.bookingms.domain.portaggregate.specification;

import com.practicalddd.cargotracker.bookingms.domain.specification.Specification;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.PortEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Expression;

/**
 * Specifications para entidade JPA PortEntity
 * Usa Criteria API para consultas no banco de dados
 */
public class PortJpaSpecification {
    
    public static Specification<PortEntity> congestedPorts() {
        return new Specification<PortEntity>() {
            @Override
            public Predicate toPredicate(Root<PortEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                // Calcula percentage: (currentUsage * 100.0 / maxCapacity)
                // Primeiro, multiplica currentUsage por 100.0
                Expression<Double> multipliedUsage = cb.prod(
                    root.get("currentUsage").as(Double.class), 
                    cb.literal(100.0)
                );
                
                // Divide o resultado por maxCapacity
                Expression<Number> percentage = cb.quot(
                    multipliedUsage, 
                    root.get("maxCapacity").as(Double.class)
                );

                // Compara com 80.0
                return cb.gt(percentage, 80.0);
            }
            
            @Override
            public boolean isSatisfiedBy(PortEntity entity) {
                // Implementação em memória para testes
                return entity != null && (entity.getCurrentUsage() * 100.0 / entity.getMaxCapacity()) > 80;
            }
        };
    }
    
    public static Specification<PortEntity> byCountry(String country) {
        return new Specification<PortEntity>() {
            @Override
            public Predicate toPredicate(Root<PortEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(cb.upper(root.get("country")), country.toUpperCase());
            }
            
            @Override
            public boolean isSatisfiedBy(PortEntity entity) {
                return entity != null && entity.getCountry().equalsIgnoreCase(country);
            }
        };
    }
    
    public static Specification<PortEntity> operationalPorts() {
        return new Specification<PortEntity>() {
            @Override
            public Predicate toPredicate(Root<PortEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get("status"), "OPERATIONAL");
            }
            
            @Override
            public boolean isSatisfiedBy(PortEntity entity) {
                return entity != null && "OPERATIONAL".equals(entity.getStatus());
            }
        };
    }
    
    public static Specification<PortEntity> withAvailableCapacity(int requiredCapacity) {
        return new Specification<PortEntity>() {
            @Override
            public Predicate toPredicate(Root<PortEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                // maxCapacity - currentUsage >= requiredCapacity
                Expression<Integer> availableCapacity = cb.diff(
                    root.get("maxCapacity").as(Integer.class), 
                    root.get("currentUsage").as(Integer.class)
                );
                
                return cb.greaterThanOrEqualTo(
                    availableCapacity, 
                    cb.literal(requiredCapacity)
                );
            }
            
            @Override
            public boolean isSatisfiedBy(PortEntity entity) {
                return entity != null && 
                       (entity.getMaxCapacity() - entity.getCurrentUsage()) >= requiredCapacity;
            }
        };
    }
}
