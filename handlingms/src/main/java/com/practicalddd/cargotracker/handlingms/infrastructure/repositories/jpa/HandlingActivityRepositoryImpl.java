package com.practicalddd.cargotracker.handlingms.infrastructure.repositories.jpa;

import com.practicalddd.cargotracker.handlingms.domain.model.aggregates.HandlingActivity;
import com.practicalddd.cargotracker.handlingms.domain.model.repositories.HandlingActivityRepository;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.CargoBookingId;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.HandlingActivityHistory;
import com.practicalddd.cargotracker.handlingms.infrastructure.persistence.jpa.entities.HandlingActivityJPA;
import com.practicalddd.cargotracker.handlingms.infrastructure.persistence.mappers.HandlingActivityMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class HandlingActivityRepositoryImpl implements HandlingActivityRepository {
    
    private static final Logger logger = Logger.getLogger(HandlingActivityRepositoryImpl.class.getName());

    @PersistenceContext(unitName = "handlingms")
    private EntityManager entityManager;

    @Override
    @Transactional
    public void store(HandlingActivity handlingActivity) {
        try {
            logger.info("Storing handling activity for booking: " + handlingActivity.getCargoBookingId().getBookingId());
            
            HandlingActivityJPA jpaEntity = HandlingActivityMapper.toJPA(handlingActivity);
            entityManager.persist(jpaEntity);
            
            logger.info("Successfully stored handling activity with ID: " + jpaEntity.getId());
        } catch (Exception e) {
            logger.severe("Error storing handling activity: " + e.getMessage());
            throw new RuntimeException("Failed to store handling activity", e);
        }
    }

    @Override
    public HandlingActivityHistory lookupHandlingHistoryOfCargo(CargoBookingId cargoBookingId) {
        try {
            logger.info("Looking up handling history for booking: " + cargoBookingId.getBookingId());
            
            List<HandlingActivityJPA> jpaList = entityManager.createNamedQuery(
                    "HandlingActivityJPA.findByBookingId", HandlingActivityJPA.class)
                    .setParameter("bookingId", cargoBookingId.getBookingId())
                    .getResultList();

            List<HandlingActivity> handlingActivities = jpaList.stream()
                    .map(HandlingActivityMapper::toDomain)
                    .collect(Collectors.toList());

            logger.info("Found " + handlingActivities.size() + " handling activities");
            return new HandlingActivityHistory(handlingActivities);
        } catch (Exception e) {
            logger.severe("Error looking up handling history: " + e.getMessage());
            throw new RuntimeException("Failed to lookup handling history", e);
        }
    }
}
