package com.practicalddd.cargotracker.bookingms.infrastructure.repositories.readmodels;

import com.practicalddd.cargotracker.bookingms.application.readmodels.CargoReadModel;
import com.practicalddd.cargotracker.bookingms.domain.specification.Specification;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.CargoEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class CargoReadModelRepository {
    private static final Logger logger = Logger.getLogger(CargoReadModelRepository.class.getName());
    
    @PersistenceContext(unitName = "bookingms")
    private EntityManager entityManager;
    
    public List<CargoReadModel> findAll() {
        String jpql = "SELECT NEW com.practicalddd.cargotracker.bookingms.application.readmodels.CargoReadModel(" +
                     "c.bookingId, c.originLocation, c.destLocation, 'BOOKED', " +
                     "c.bookingAmount, c.destArrivalDeadline, " +
                     "CASE WHEN c.destArrivalDeadline < CURRENT_TIMESTAMP + 7 DAYS THEN true ELSE false END, " +
                     "SIZE(c.legs), c.createdAt) " +
                     "FROM CargoEntity c ORDER BY c.createdAt DESC";
        
        return entityManager.createQuery(jpql, CargoReadModel.class).getResultList();
    }
    
    public List<CargoReadModel> findBySpecification(Specification<CargoReadModel> specification) {
        // Implementação com Criteria API para Read Models
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CargoReadModel> cq = cb.createQuery(CargoReadModel.class);
        Root<CargoEntity> root = cq.from(CargoEntity.class);
        
        // Construir a projeção
        cq.select(cb.construct(CargoReadModel.class,
            root.get("bookingId"),
            root.get("originLocation"),
            root.get("destLocation"),
            cb.literal("BOOKED"), // Status simplificado
            root.get("bookingAmount"),
            root.get("destArrivalDeadline"),
            cb.lessThan(root.get("destArrivalDeadline"), 
                       cb.currentTimestamp().as(LocalDateTime.class)),
            cb.size(root.get("legs")),
            root.get("createdAt")
        ));
        
        TypedQuery<CargoReadModel> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
    
    public List<CargoReadModel> findUrgentCargos() {
        String jpql = "SELECT NEW com.practicalddd.cargotracker.bookingms.application.readmodels.CargoReadModel(" +
                     "c.bookingId, c.originLocation, c.destLocation, 'BOOKED', " +
                     "c.bookingAmount, c.destArrivalDeadline, true, " +
                     "SIZE(c.legs), c.createdAt) " +
                     "FROM CargoEntity c " +
                     "WHERE c.destArrivalDeadline < CURRENT_TIMESTAMP + 7 DAYS " +
                     "ORDER BY c.destArrivalDeadline ASC";
        
        return entityManager.createQuery(jpql, CargoReadModel.class).getResultList();
    }
}
