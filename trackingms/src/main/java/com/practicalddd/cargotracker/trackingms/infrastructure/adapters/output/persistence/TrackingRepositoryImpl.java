package com.practicalddd.cargotracker.trackingms.infrastructure.adapters.output.persistence;

import com.practicalddd.cargotracker.trackingms.application.ports.output.TrackingRepository;
import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingActivity;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingNumber;
import com.practicalddd.cargotracker.trackingms.infrastructure.adapters.output.persistence.jpa.TrackingActivityJpa;
import com.practicalddd.cargotracker.trackingms.infrastructure.adapters.output.persistence.mapper.TrackingPersistenceMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class TrackingRepositoryImpl implements TrackingRepository {

    private static final Logger logger = Logger.getLogger(TrackingRepositoryImpl.class.getName());

    @PersistenceContext(unitName = "trackingms")
    private EntityManager entityManager;

    @Inject
    private TrackingPersistenceMapper mapper;

    @Override
    public Optional<TrackingActivity> findByTrackingNumber(TrackingNumber trackingNumber) {
        try {
            TrackingActivityJpa jpaEntity = entityManager
                    .createNamedQuery("TrackingActivityJpa.findByTrackingNumber", TrackingActivityJpa.class)
                    .setParameter("trackingNumber", trackingNumber.getTrackingNumber())
                    .getSingleResult();

            TrackingActivity domain = mapper.toDomain(jpaEntity);
            return Optional.ofNullable(domain);

        } catch (Exception e) {
            logger.fine("No TrackingActivity found for tracking number: " +
                    trackingNumber.getTrackingNumber());
            return Optional.empty();
        }
    }

    @Override
    public Optional<TrackingActivity> findByBookingId(BookingId bookingId) {
        logger.info("Buscando TrackingActivity para bookingId: " + bookingId.getBookingId());

        try {
            TrackingActivityJpa jpaEntity = entityManager
                    .createNamedQuery("TrackingActivityJpa.findByBookingId", TrackingActivityJpa.class)
                    .setParameter("bookingId", bookingId.getBookingId())
                    .getSingleResult();

            logger.info("Encontrado TrackingActivityJpa: " + jpaEntity.getId());

            TrackingActivity domain = mapper.toDomain(jpaEntity);
            return Optional.ofNullable(domain);

        } catch (NoResultException e) {
            logger.info("Nenhuma TrackingActivity encontrada para bookingId: " + bookingId.getBookingId());
            return Optional.empty();
        } catch (Exception e) {
            logger.severe("Erro ao buscar TrackingActivity: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public TrackingActivity save(TrackingActivity trackingActivity) {
        logger.info("Salvando TrackingActivity para booking: " +
                trackingActivity.getBookingId().getBookingId());

        try {
            // Converter para JPA (o mapper cuida de buscar existente ou criar nova)
            TrackingActivityJpa jpaEntity = mapper.toJpa(trackingActivity);

            logger.info("JPA Entity convertida, trackingNumber: " + jpaEntity.getTrackingNumber() +
                       ", ID: " + jpaEntity.getId());

            // Se é nova entidade (sem ID), persistir
            if (jpaEntity.getId() == null) {
                logger.info("Persistindo nova entidade...");
                entityManager.persist(jpaEntity);
                entityManager.flush();
                logger.info("Entidade persistida com ID: " + jpaEntity.getId());
            } else {
                logger.info("Atualizando entidade existente ID: " + jpaEntity.getId());
                // Entidade gerenciada (foi buscada pelo mapper), 
                // o JPA detectará mudanças automaticamente
            }

            // Converter de volta para domínio (com ID atualizado se for novo)
            TrackingActivity savedActivity = mapper.toDomain(jpaEntity);

            logger.info("TrackingActivity salva com sucesso. ID: " + savedActivity.getId());
            return savedActivity;

        } catch (Exception e) {
            logger.severe("ERRO CRÍTICO ao salvar TrackingActivity: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save TrackingActivity", e);
        }
    }

    @Override
    public String generateNextTrackingNumber() {
        String random = UUID.randomUUID().toString().toUpperCase();
        return "TRK-" + random.substring(0, random.indexOf("-"));
    }

    @Override
    public List<TrackingActivity> findAll() {
        return entityManager
                .createNamedQuery("TrackingActivityJpa.findAll", TrackingActivityJpa.class)
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrackingNumber> findAllTrackingNumbers() {
        return entityManager
                .createQuery("SELECT DISTINCT t.trackingNumber FROM TrackingActivityJpa t", String.class)
                .getResultList()
                .stream()
                .map(TrackingNumber::new)
                .collect(Collectors.toList());
    }
}
