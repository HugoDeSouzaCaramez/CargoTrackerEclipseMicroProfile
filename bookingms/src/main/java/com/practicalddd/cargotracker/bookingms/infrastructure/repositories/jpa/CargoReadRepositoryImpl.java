package com.practicalddd.cargotracker.bookingms.infrastructure.repositories.jpa;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.CargoView;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.CargoReadRepository;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class CargoReadRepositoryImpl implements CargoReadRepository {
    
    private static final Logger logger = Logger.getLogger(CargoReadRepositoryImpl.class.getName());
    
    @PersistenceContext(unitName = "bookingms")
    private EntityManager entityManager;
    
    @Override
    public Optional<CargoView> findViewByBookingId(BookingId bookingId) {
        try {
            CargoView view = entityManager.find(CargoView.class, bookingId.getBookingId());
            return Optional.ofNullable(view);
        } catch (Exception e) {
            logger.severe("Error finding cargo view by booking ID: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<CargoView> findViewByBookingId(String bookingId) {
        try {
            CargoView view = entityManager.find(CargoView.class, bookingId);
            return Optional.ofNullable(view);
        } catch (Exception e) {
            logger.severe("Error finding cargo view by booking ID string: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public List<CargoView> findAllViews() {
        try {
            TypedQuery<CargoView> query = entityManager.createQuery(
                "SELECT cv FROM CargoView cv ORDER BY cv.lastUpdated DESC", CargoView.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding all cargo views: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<CargoView> findViewsByStatus(String status) {
        try {
            TypedQuery<CargoView> query = entityManager.createQuery(
                "SELECT cv FROM CargoView cv WHERE cv.status = :status ORDER BY cv.lastUpdated DESC", 
                CargoView.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding cargo views by status: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<CargoView> findViewsByOrigin(String originLocation) {
        try {
            TypedQuery<CargoView> query = entityManager.createQuery(
                "SELECT cv FROM CargoView cv WHERE cv.originLocation = :originLocation ORDER BY cv.lastUpdated DESC", 
                CargoView.class);
            query.setParameter("originLocation", originLocation);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding cargo views by origin: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<CargoView> findViewsByDestination(String destinationLocation) {
        try {
            TypedQuery<CargoView> query = entityManager.createQuery(
                "SELECT cv FROM CargoView cv WHERE cv.destinationLocation = :destinationLocation ORDER BY cv.lastUpdated DESC", 
                CargoView.class);
            query.setParameter("destinationLocation", destinationLocation);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding cargo views by destination: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<CargoView> findViewsWithUpcomingDeadline(int daysAhead) {
        try {
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(daysAhead);
            
            TypedQuery<CargoView> query = entityManager.createQuery(
                "SELECT cv FROM CargoView cv WHERE cv.arrivalDeadline BETWEEN :startDate AND :endDate ORDER BY cv.arrivalDeadline ASC", 
                CargoView.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding cargo views with upcoming deadline: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    @Transactional
    public void saveView(CargoView cargoView) {
        try {
            if (cargoView.getBookingId() == null) {
                throw new IllegalArgumentException("CargoView booking ID cannot be null");
            }
            
            // Verifica se já existe
            CargoView existing = entityManager.find(CargoView.class, cargoView.getBookingId());
            
            if (existing != null) {
                // Atualiza a versão existente
                cargoView.setVersion(existing.getVersion() + 1);
                entityManager.merge(cargoView);
                logger.fine("Updated cargo view: " + cargoView.getBookingId());
            } else {
                // Insere novo
                entityManager.persist(cargoView);
                logger.fine("Created new cargo view: " + cargoView.getBookingId());
            }
            
            entityManager.flush();
            
        } catch (Exception e) {
            logger.severe("Error saving cargo view: " + e.getMessage());
            throw new RuntimeException("Failed to save cargo view: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void deleteView(BookingId bookingId) {
        try {
            CargoView view = entityManager.find(CargoView.class, bookingId.getBookingId());
            if (view != null) {
                entityManager.remove(view);
                logger.fine("Deleted cargo view: " + bookingId.getBookingId());
            }
        } catch (Exception e) {
            logger.severe("Error deleting cargo view: " + e.getMessage());
            throw new RuntimeException("Failed to delete cargo view", e);
        }
    }
    
    @Override
    public long countAllViews() {
        try {
            Long count = entityManager.createQuery("SELECT COUNT(cv) FROM CargoView cv", Long.class)
                    .getSingleResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.severe("Error counting cargo views: " + e.getMessage());
            return 0;
        }
    }
    
    @Override
    public List<CargoView> findViewsPaginated(int page, int size) {
        try {
            TypedQuery<CargoView> query = entityManager.createQuery(
                "SELECT cv FROM CargoView cv ORDER BY cv.lastUpdated DESC", CargoView.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding paginated cargo views: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<CargoView> findViewsByStatuses(List<String> statuses) {
        try {
            if (statuses == null || statuses.isEmpty()) {
                return Collections.emptyList();
            }
            
            TypedQuery<CargoView> query = entityManager.createQuery(
                "SELECT cv FROM CargoView cv WHERE cv.status IN :statuses ORDER BY cv.lastUpdated DESC", 
                CargoView.class);
            query.setParameter("statuses", statuses);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding cargo views by multiple statuses: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<CargoView> findViewsByOriginAndDestination(String origin, String destination) {
        try {
            TypedQuery<CargoView> query = entityManager.createQuery(
                "SELECT cv FROM CargoView cv WHERE cv.originLocation = :origin AND cv.destinationLocation = :destination " +
                "ORDER BY cv.lastUpdated DESC", CargoView.class);
            query.setParameter("origin", origin);
            query.setParameter("destination", destination);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding cargo views by origin and destination: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
