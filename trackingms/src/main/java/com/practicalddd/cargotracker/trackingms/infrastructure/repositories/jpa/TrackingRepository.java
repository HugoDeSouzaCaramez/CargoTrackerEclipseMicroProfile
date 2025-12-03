package com.practicalddd.cargotracker.trackingms.infrastructure.repositories.jpa;

import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingActivity;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingNumber;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class TrackingRepository {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(
            TrackingRepository.class.getName());

    @PersistenceContext(unitName = "trackingms")
    private EntityManager entityManager;

    public TrackingActivity find(TrackingNumber trackingNumber) {
        TrackingActivity trackingActivity;
        try {
            trackingActivity = entityManager.createNamedQuery("TrackingActivity.findByTrackingNumber",
                    TrackingActivity.class)
                    .setParameter("trackingNumber", trackingNumber)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.log(Level.FINE, "Find called on non-existant Tracking No.", e);
            trackingActivity = null;
        }

        return trackingActivity;
    }

    public TrackingActivity find(BookingId bookingId) {
        TrackingActivity trackingActivity;
        try {
            trackingActivity = entityManager.createNamedQuery("TrackingActivity.findByBookingNumber",
                    TrackingActivity.class)
                    .setParameter("bookingId", bookingId)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.log(Level.FINE, "Find called on non-existant Booking ID: " + bookingId.getBookingId(), e);
            trackingActivity = null;
        }
        return trackingActivity;
    }

    public void store(TrackingActivity trackingActivity) {
        entityManager.persist(trackingActivity);
        entityManager.flush();
    }

    public String nextTrackingNumber() {
        String random = UUID.randomUUID().toString().toUpperCase();
        return random.substring(0, random.indexOf("-"));
    }

    public List<TrackingActivity> findAll() {
        return entityManager.createNamedQuery("TrackingActivity.findAll", TrackingActivity.class)
                .getResultList();
    }

    public List<TrackingNumber> findAllTrackingNumbers() {
        List<TrackingNumber> trackingNumbers = new ArrayList<TrackingNumber>();

        try {
            trackingNumbers = entityManager.createNamedQuery(
                    "TrackingActivity.getAllTrackingNos", TrackingNumber.class).getResultList();
        } catch (NoResultException e) {
            logger.log(Level.FINE, "Unable to get all tracking numbers", e);
        }

        return trackingNumbers;
    }

    @PostConstruct
    public void checkDatabaseConnection() {
        try {
            List<TrackingActivity> activities = findAll();
            logger.info("✅ TrackingMS database connection OK. Found " + activities.size() + " existing records");
        } catch (Exception e) {
            logger.severe("❌ TrackingMS database connection FAILED: " + e.getMessage());
        }
    }
}
