package com.practicalddd.cargotracker.bookingms.domain.specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Classe base para Specifications que serão usadas apenas em memória.
 * Implementa toPredicate() lançando exceção já que não é para uso com JPA.
 */
public abstract class InMemorySpecification<T> implements Specification<T> {
    
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        throw new UnsupportedOperationException(
            "Esta specification é apenas para avaliação em memória. " +
            "Use uma implementação JPA para consultas ao banco."
        );
    }
}
