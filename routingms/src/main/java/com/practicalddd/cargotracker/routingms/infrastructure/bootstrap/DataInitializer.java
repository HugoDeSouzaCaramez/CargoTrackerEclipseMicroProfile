package com.practicalddd.cargotracker.routingms.infrastructure.bootstrap;

import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.routingms.domain.model.entities.CarrierMovement;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Schedule;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.VoyageNumber;
import com.practicalddd.cargotracker.routingms.infrastructure.repositories.jpa.VoyageRepository;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class DataInitializer {

    private static final Logger logger = Logger.getLogger(DataInitializer.class.getName());

    @Inject
    private VoyageRepository voyageRepository;

    @PostConstruct
    @Transactional
    public void init() {
        logger.info("🎯 === INITIALIZING ROUTING DATABASE ===");

        try {
            List<Voyage> existingVoyages = voyageRepository.findAll();
            logger.info("📊 Found " + existingVoyages.size() + " existing voyages in database");

            if (existingVoyages.isEmpty()) {
                logger.info("🆕 Creating sample voyages...");
                createSampleVoyages();

                // Verifica se os dados foram criados
                List<Voyage> afterCreation = voyageRepository.findAll();
                logger.info("✅ After initialization: " + afterCreation.size() + " voyages in database");

                // Log detalhado dos voyages criados
                for (Voyage voyage : afterCreation) {
                    List<CarrierMovement> movements = voyage.getSchedule().getCarrierMovements();
                    if (!movements.isEmpty()) {
                        CarrierMovement mov = movements.get(0);
                        logger.info("🚢 Voyage " + voyage.getVoyageNumber().getVoyageNumber() +
                                ": " + mov.getDepartureLocation().getUnLocCode() +
                                " → " + mov.getArrivalLocation().getUnLocCode() +
                                " (" + mov.getDepartureDate() + " to " + mov.getArrivalDate() + ")");
                    }
                }
            } else {
                logger.info("ℹ️ Database already populated with " + existingVoyages.size() + " voyages");
            }
        } catch (Exception e) {
            logger.severe("❌ Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createSampleVoyages() {
        // Voyage de Tokyo para Singapore
        Voyage voyage1 = createVoyage("V0100", "JPTYO", "SGSIN",
                createDate(2024, Calendar.JANUARY, 10),
                createDate(2024, Calendar.JANUARY, 14));
        voyageRepository.store(voyage1);

        // Voyage de Tokyo para Singapore (alternativo)
        Voyage voyage2 = createVoyage("V0200", "JPTYO", "SGSIN",
                createDate(2024, Calendar.JANUARY, 12),
                createDate(2024, Calendar.JANUARY, 16));
        voyageRepository.store(voyage2);

        // Voyage de Singapore para Tokyo
        Voyage voyage3 = createVoyage("V0300", "SGSIN", "JPTYO",
                createDate(2024, Calendar.JANUARY, 20),
                createDate(2024, Calendar.JANUARY, 24));
        voyageRepository.store(voyage3);

        // Outras rotas comuns
        Voyage voyage4 = createVoyage("V0400", "USNYC", "GBLON",
                createDate(2024, Calendar.JANUARY, 5),
                createDate(2024, Calendar.JANUARY, 6));
        voyageRepository.store(voyage4);

        Voyage voyage5 = createVoyage("V0500", "CNHKG", "JPTYO",
                createDate(2024, Calendar.JANUARY, 8),
                createDate(2024, Calendar.JANUARY, 10));
        voyageRepository.store(voyage5);
    }

    private Voyage createVoyage(String voyageNumber, String from, String to, Date departure, Date arrival) {
        List<CarrierMovement> carrierMovements = new ArrayList<>();
        carrierMovements.add(new CarrierMovement(
                new Location(from),
                new Location(to),
                departure,
                arrival));
        Schedule schedule = new Schedule(carrierMovements);
        return new Voyage(new VoyageNumber(voyageNumber), schedule);
    }

    private Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}