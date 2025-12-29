package com.practicalddd.cargotracker.bookingms.infrastructure.repositories.jpa;

import com.practicalddd.cargotracker.bookingms.domain.portaggregate.Port;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.repositories.PortRepository;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortId;
import com.practicalddd.cargotracker.bookingms.domain.specification.Specification;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.PortEntity;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.mappers.PortMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class PortRepositoryImpl implements PortRepository {

    private static final Logger logger = Logger.getLogger(PortRepositoryImpl.class.getName());

    @PersistenceContext(unitName = "bookingms")
    private EntityManager entityManager;

    @Inject
    private PortMapper portMapper;

    @Override
    public Optional<Port> findById(PortId portId) {
        try {
            PortEntity entity = entityManager.find(PortEntity.class, portId.getUnLocCode());
            return entity != null ? Optional.of(portMapper.toDomain(entity)) : Optional.empty();
        } catch (Exception e) {
            logger.warning("Error finding port by ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Port> findByUnLocCode(String unLocCode) {
        try {
            PortEntity entity = entityManager.createQuery(
                    "SELECT p FROM PortEntity p WHERE p.unLocCode = :unLocCode", PortEntity.class)
                    .setParameter("unLocCode", unLocCode.toUpperCase())
                    .getSingleResult();
            return Optional.of(portMapper.toDomain(entity));
        } catch (NoResultException e) {
            logger.fine("Port not found: " + unLocCode);
            return Optional.empty();
        } catch (Exception e) {
            logger.severe("Error finding port by UN/LOCODE: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<Port> findAll() {
        try {
            List<PortEntity> entities = entityManager.createQuery(
                    "SELECT p FROM PortEntity p ORDER BY p.name", PortEntity.class)
                    .getResultList();
            return entities.stream()
                    .map(portMapper::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.severe("Error finding all ports: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Port> findByCountry(String country) {
        try {
            List<PortEntity> entities = entityManager.createQuery(
                    "SELECT p FROM PortEntity p WHERE UPPER(p.country) = UPPER(:country) ORDER BY p.name",
                    PortEntity.class)
                    .setParameter("country", country)
                    .getResultList();
            return entities.stream()
                    .map(portMapper::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.severe("Error finding ports by country: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Port> findCongestedPorts() {
        try {
            List<PortEntity> entities = entityManager.createQuery(
                    "SELECT p FROM PortEntity p WHERE (p.currentUsage * 100.0 / p.maxCapacity) > 80 ORDER BY (p.currentUsage * 100.0 / p.maxCapacity) DESC",
                    PortEntity.class)
                    .getResultList();
            return entities.stream()
                    .map(portMapper::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.severe("Error finding congested ports: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public void save(Port port) {
        try {
            logger.fine(() -> "Saving port: " + port.getUnLocCode());

            // Usa o mapper que já lida com atualização/criação
            PortEntity entity = portMapper.toEntity(port);

            if (entity.getUnLocCode() == null) {
                throw new IllegalArgumentException("Port UN/LOCODE cannot be null");
            }

            // Verifica se é uma nova entidade ou atualização
            PortEntity existing = entityManager.find(PortEntity.class, entity.getUnLocCode());

            if (existing != null) {
                // Merge mantém as datas corretamente
                entityManager.merge(entity);
                logger.fine(() -> "Updated port: " + port.getUnLocCode());
            } else {
                entityManager.persist(entity);
                logger.fine(() -> "Created new port: " + port.getUnLocCode());
            }

            // Forçar flush para detectar erros mais cedo
            entityManager.flush();

        } catch (Exception e) {
            logger.severe("Error saving port " + port.getUnLocCode() + ": " + e.getMessage());
            throw new RuntimeException("Failed to save port: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(PortId portId) {
        try {
            PortEntity entity = entityManager.find(PortEntity.class, portId.getUnLocCode());
            if (entity != null) {
                entityManager.remove(entity);
                logger.fine(() -> "Deleted port: " + portId.getUnLocCode());
            }
        } catch (Exception e) {
            logger.severe("Error deleting port: " + e.getMessage());
            throw new RuntimeException("Failed to delete port", e);
        }
    }

    @Override
    public boolean exists(PortId portId) {
        try {
            return entityManager.find(PortEntity.class, portId.getUnLocCode()) != null;
        } catch (Exception e) {
            logger.warning("Error checking if port exists: " + e.getMessage());
            return false;
        }
    }

    // Atualizar PortRepositoryImpl.java - adicionar novos métodos
    @Override
    public List<Port> findAll(Specification<Port> specification) {
        try {
            List<Port> allPorts = findAll();
            return allPorts.stream()
                    .filter(port -> specification.isSatisfiedBy(port))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.severe("Error finding ports by specification: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // NOVO: Método para usar Specifications JPA diretamente
    public List<Port> findAllBySpecification(Specification<PortEntity> specification) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<PortEntity> cq = cb.createQuery(PortEntity.class);
            Root<PortEntity> root = cq.from(PortEntity.class);

            Predicate predicate = specification.toPredicate(root, cq, cb);
            cq.where(predicate);

            TypedQuery<PortEntity> query = entityManager.createQuery(cq);
            List<PortEntity> entities = query.getResultList();

            return entities.stream()
                    .map(portMapper::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.severe("Error finding ports by JPA specification: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
