package com.practicalddd.cargotracker.routingms.infrastructure.repositories.jpa;

import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.routingms.domain.model.repositories.VoyageRepository;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.VoyageNumber;
import com.practicalddd.cargotracker.routingms.infrastructure.persistence.jpa.entities.VoyageEntity;
import com.practicalddd.cargotracker.routingms.infrastructure.persistence.mappers.VoyageMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class VoyageRepositoryImpl implements VoyageRepository {

    @PersistenceContext(unitName = "routingms")
    private EntityManager entityManager;

    @Override
    public Optional<Voyage> findByVoyageNumber(VoyageNumber voyageNumber) {
        try {
            VoyageEntity entity = entityManager.createNamedQuery("VoyageEntity.findByVoyageNumber", VoyageEntity.class)
                    .setParameter("voyageNumber", voyageNumber.getVoyageNumber())
                    .getSingleResult();
            
            // Force initialization of lazy collection
            if (entity.getCarrierMovements() != null) {
                entity.getCarrierMovements().size();
            }
            
            return Optional.of(VoyageMapper.toDomain(entity));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Voyage> findAll() {
        List<VoyageEntity> entities = entityManager.createNamedQuery("VoyageEntity.findAll", VoyageEntity.class)
                .getResultList();
        
        // Force initialization of lazy collections
        entities.forEach(entity -> {
            if (entity.getCarrierMovements() != null) {
                entity.getCarrierMovements().size();
            }
        });
        
        return entities.stream()
                .map(VoyageMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void store(Voyage voyage) {
        VoyageEntity entity = VoyageMapper.toEntity(voyage);
        
        try {
            // Check if exists
            VoyageEntity existing = entityManager.createNamedQuery("VoyageEntity.findByVoyageNumber", VoyageEntity.class)
                    .setParameter("voyageNumber", voyage.getVoyageNumber().getVoyageNumber())
                    .getSingleResult();
            
            // Update existing
            existing.setCarrierMovements(entity.getCarrierMovements());
            entityManager.merge(existing);
        } catch (NoResultException e) {
            // Persist new
            entityManager.persist(entity);
        }
    }
}
