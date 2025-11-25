package com.practicalddd.cargotracker.bookingms.infrastructure.repositories.jpa;

import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class CargoRepositoryImpl implements CargoRepository {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(CargoRepositoryImpl.class.getName());

    @PersistenceContext(unitName = "bookingms")
    private EntityManager entityManager;

    @Inject
    private UserTransaction userTransaction;

    @Override
    public Cargo find(BookingId bookingId) {
        Cargo cargo;
        try {
            cargo = entityManager.createNamedQuery("Cargo.findByBookingId", Cargo.class)
                    .setParameter("bookingId", bookingId)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.log(Level.FINE, "Find called on non-existant Booking ID.", e);
            cargo = null;
        }
        return cargo;
    }

    @Override
    @Transactional
    public void store(Cargo cargo) {
        entityManager.persist(cargo);
    }

    @Override
    public String nextBookingId() {
        String random = UUID.randomUUID().toString().toUpperCase();
        return random.substring(0, random.indexOf("-"));
    }

    @Override
    public List<Cargo> findAll() {
        return entityManager.createNamedQuery("Cargo.findAll", Cargo.class)
                .getResultList();
    }

    @Override
    public List<BookingId> findAllBookingIds() {
        List<BookingId> bookingIds = new ArrayList<>();
        try {
            bookingIds = entityManager.createNamedQuery("Cargo.getAllBookingIds", BookingId.class)
                    .getResultList();
        } catch (NoResultException e) {
            logger.log(Level.FINE, "Unable to get all booking IDs", e);
        }
        return bookingIds;
    }

    public void storeManualTx(Cargo cargo) throws Exception {
        try {
            userTransaction.begin();
            entityManager.persist(cargo);
            userTransaction.commit();
        } catch (Exception e) {
            if (userTransaction.getStatus() == javax.transaction.Status.STATUS_ACTIVE) {
                userTransaction.rollback();
            }
            throw e;
        }
    }
}