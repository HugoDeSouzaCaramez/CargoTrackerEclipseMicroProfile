package com.practicalddd.cargotracker.bookingms.domain.specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class Specifications {
    
    public static <T> Specification<T> and(Specification<T> left, Specification<T> right) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.and(
                    left.toPredicate(root, query, criteriaBuilder),
                    right.toPredicate(root, query, criteriaBuilder)
                );
            }
            
            @Override
            public boolean isSatisfiedBy(T entity) {
                return left.isSatisfiedBy(entity) && right.isSatisfiedBy(entity);
            }
        };
    }
    
    public static <T> Specification<T> or(Specification<T> left, Specification<T> right) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.or(
                    left.toPredicate(root, query, criteriaBuilder),
                    right.toPredicate(root, query, criteriaBuilder)
                );
            }
            
            @Override
            public boolean isSatisfiedBy(T entity) {
                return left.isSatisfiedBy(entity) || right.isSatisfiedBy(entity);
            }
        };
    }
    
    public static <T> Specification<T> not(Specification<T> spec) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.not(spec.toPredicate(root, query, criteriaBuilder));
            }
            
            @Override
            public boolean isSatisfiedBy(T entity) {
                return !spec.isSatisfiedBy(entity);
            }
        };
    }
    
    public static <T> Specification<T> where(Specification<T> spec) {
        return spec;
    }
}
