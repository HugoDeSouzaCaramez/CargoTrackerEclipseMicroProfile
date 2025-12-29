package com.practicalddd.cargotracker.bookingms.domain.specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Interface para o padrão Specification (DDD).
 * Encapsula critérios de consulta de forma reutilizável e composta.
 * 
 * @param <T> Tipo da entidade do domínio
 */
public interface Specification<T> {
    
    /**
     * Converte a specification em Predicate JPA para consultas no banco
     */
    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
    
    /**
     * Verifica se uma entidade satisfaz a specification (para validação em memória)
     */
    default boolean isSatisfiedBy(T entity) {
        throw new UnsupportedOperationException("Implementação em memória não suportada para esta specification");
    }
}
