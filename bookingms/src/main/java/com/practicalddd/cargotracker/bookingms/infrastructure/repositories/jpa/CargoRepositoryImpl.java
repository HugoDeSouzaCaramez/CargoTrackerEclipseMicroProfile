package com.practicalddd.cargotracker.bookingms.infrastructure.repositories.jpa;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.specification.Specification;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.CargoEntity;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.LegEntity;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.mappers.CargoMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class CargoRepositoryImpl implements CargoRepository {
    private static final Logger logger = Logger.getLogger(CargoRepositoryImpl.class.getName());
    private static final AtomicInteger counter = new AtomicInteger(1);
    
    @PersistenceContext(unitName = "bookingms")
    private EntityManager entityManager;
    
    @Override
    public Optional<Cargo> find(BookingId bookingId) {
        try {
            CargoEntity entity = entityManager.createNamedQuery("CargoEntity.findByBookingId", CargoEntity.class)
                    .setParameter("bookingId", bookingId.getBookingId())
                    .getSingleResult();
            
            // Carregar as legs explicitamente para evitar LazyInitializationException
            if (entity != null && entity.getLegs() != null) {
                entity.getLegs().size(); // Force initialization
            }
            
            return Optional.of(CargoMapper.toDomain(entity));
        } catch (NoResultException e) {
            logger.log(Level.FINE, "Find called on non-existant Booking ID.", e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void store(Cargo cargo) {
        CargoEntity entity = CargoMapper.toEntity(cargo);
        
        // Check if exists
        try {
            CargoEntity existing = entityManager.createNamedQuery("CargoEntity.findByBookingId", CargoEntity.class)
                    .setParameter("bookingId", cargo.getBookingId().getBookingId())
                    .getSingleResult();
            
            // Carregar as legs existentes para evitar problemas de merge
            if (existing.getLegs() != null) {
                existing.getLegs().size(); // Force initialization
            }
            
            // Atualizar os dados básicos
            existing.setBookingAmount(entity.getBookingAmount());
            existing.setOriginLocation(entity.getOriginLocation());
            existing.setDestLocation(entity.getDestLocation());
            existing.setDestArrivalDeadline(entity.getDestArrivalDeadline());
            
            // Limpar legs existentes e adicionar as novas
            existing.getLegs().clear();
            if (entity.getLegs() != null) {
                for (LegEntity leg : entity.getLegs()) {
                    LegEntity newLeg = new LegEntity(
                        leg.getVoyageNumber(),
                        leg.getLoadLocation(),
                        leg.getUnloadLocation(),
                        leg.getLoadTime(),
                        leg.getUnloadTime()
                    );
                    newLeg.setCargo(existing);
                    existing.getLegs().add(newLeg);
                }
            }
            
            entityManager.merge(existing);
        } catch (NoResultException e) {
            // Para nova entidade, garantir que as legs tenham referência ao cargo
            if (entity.getLegs() != null) {
                for (LegEntity leg : entity.getLegs()) {
                    leg.setCargo(entity);
                }
            }
            entityManager.persist(entity);
        }
    }

    @Override
    public String nextBookingId() {
        // Padrão: CAR + timestamp + sequencial
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000000); // últimos 6 dígitos
        String sequence = String.format("%03d", counter.getAndIncrement() % 1000);
        
        // Resetar contador se passar de 999
        if (counter.get() > 999) {
            counter.set(1);
        }
        
        String bookingId = "CAR" + timestamp + sequence;
        logger.fine(() -> "Generated booking ID: " + bookingId);
        return bookingId;
    }
    
    // Método alternativo mantendo compatibilidade
    public String nextBookingIdOld() {
        String random = UUID.randomUUID().toString().toUpperCase();
        return random.substring(0, random.indexOf("-"));
    }

    @Override
    public List<Cargo> findAll() {
        List<CargoEntity> entities = entityManager.createNamedQuery("CargoEntity.findAll", CargoEntity.class)
                .getResultList();
        
        // Carregar as legs para cada entidade
        for (CargoEntity entity : entities) {
            if (entity.getLegs() != null) {
                entity.getLegs().size(); // Force initialization
            }
        }
        
        return entities.stream()
                .map(CargoMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingId> findAllBookingIds() {
        List<String> bookingIds = entityManager.createNamedQuery("CargoEntity.getAllBookingIds", String.class)
                .getResultList();
        return bookingIds.stream()
                .map(BookingId::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Cargo> findAll(Specification<Cargo> specification) {
        try {
            // Para specifications que operam em Cargo (domínio), precisamos converter para CargoEntity
            // Vamos buscar todas e filtrar em memória para specifications simples
            // Em produção, criar uma Specification<CargoEntity> e usar Criteria API
            List<Cargo> allCargos = findAll();
            return allCargos.stream()
                    .filter(cargo -> specification.isSatisfiedBy(cargo))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding cargos by specification", e);
            return new ArrayList<>();
        }
    }
    
    // NOVO: Método para usar Specifications JPA diretamente com CargoEntity
    public List<Cargo> findAllBySpecification(com.practicalddd.cargotracker.bookingms.domain.specification.Specification<CargoEntity> specification) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<CargoEntity> cq = cb.createQuery(CargoEntity.class);
            Root<CargoEntity> root = cq.from(CargoEntity.class);
            
            Predicate predicate = specification.toPredicate(root, cq, cb);
            cq.where(predicate);
            
            TypedQuery<CargoEntity> query = entityManager.createQuery(cq);
            List<CargoEntity> entities = query.getResultList();
            
            // Carregar as legs para cada entidade
            for (CargoEntity entity : entities) {
                if (entity.getLegs() != null) {
                    entity.getLegs().size(); // Force initialization
                }
            }
            
            return entities.stream()
                    .map(CargoMapper::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding cargos by JPA specification", e);
            return new ArrayList<>();
        }
    }
    
    // NOVO: Método genérico para consultas com Criteria API
    public List<Cargo> findWithCriteria(java.util.function.Function<CriteriaBuilder, Predicate> predicateBuilder) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<CargoEntity> cq = cb.createQuery(CargoEntity.class);
            Root<CargoEntity> root = cq.from(CargoEntity.class);
            
            cq.where(predicateBuilder.apply(cb));
            
            TypedQuery<CargoEntity> query = entityManager.createQuery(cq);
            List<CargoEntity> entities = query.getResultList();
            
            for (CargoEntity entity : entities) {
                if (entity.getLegs() != null) {
                    entity.getLegs().size(); // Force initialization
                }
            }
            
            return entities.stream()
                    .map(CargoMapper::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding cargos with criteria", e);
            return new ArrayList<>();
        }
    }
}
