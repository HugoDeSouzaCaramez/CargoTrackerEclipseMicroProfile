package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.specification;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.specification.Specification;
import com.practicalddd.cargotracker.bookingms.domain.specification.Specifications;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.CargoEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;

/**
 * Specifications específicas para entidade Cargo
 */
public class CargoSpecification {
    
    public static Specification<CargoEntity> urgentCargos() {
        return new Specification<CargoEntity>() {
            @Override
            public Predicate toPredicate(Root<CargoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.lessThan(root.get("destArrivalDeadline"), LocalDateTime.now().plusDays(7));
            }
        };
    }
    
    public static Specification<CargoEntity> byStatus(String status) {
        return new Specification<CargoEntity>() {
            @Override
            public Predicate toPredicate(Root<CargoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                // Para simplicidade, assumindo que há join com delivery
                return cb.equal(root.get("status"), status);
            }
        };
    }
    
    public static Specification<CargoEntity> byOrigin(String originUnLocCode) {
        return new Specification<CargoEntity>() {
            @Override
            public Predicate toPredicate(Root<CargoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get("originLocation"), originUnLocCode);
            }
        };
    }
    
    public static Specification<CargoEntity> byDestination(String destUnLocCode) {
        return new Specification<CargoEntity>() {
            @Override
            public Predicate toPredicate(Root<CargoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get("destLocation"), destUnLocCode);
            }
        };
    }
    
    public static Specification<CargoEntity> withBookingAmountGreaterThan(int minAmount) {
        return new Specification<CargoEntity>() {
            @Override
            public Predicate toPredicate(Root<CargoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.greaterThan(root.get("bookingAmount"), minAmount);
            }
        };
    }
    
    public static Specification<CargoEntity> urgentCargosWithAvailableCapacity() {
        // Cargas urgentes com capacidade disponível no porto de origem
        return Specifications.and(
            urgentCargos(),
            new Specification<CargoEntity>() {
                @Override
                public Predicate toPredicate(Root<CargoEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    // Esta seria uma subquery complexa envolvendo PortEntity
                    return cb.isTrue(cb.literal(true)); // Placeholder
                }
            }
        );
    }
}
