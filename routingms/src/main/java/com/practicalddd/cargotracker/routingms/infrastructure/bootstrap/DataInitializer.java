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
import java.util.List;
import java.time.LocalDateTime;
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
                createDateTime(2024, 1, 10),  // Janeiro 10, 2024
                createDateTime(2024, 1, 14)); // Janeiro 14, 2024
        voyageRepository.store(voyage1);

        // Voyage de Tokyo para Singapore (alternativo)
        Voyage voyage2 = createVoyage("V0200", "JPTYO", "SGSIN",
                createDateTime(2024, 1, 12),  // Janeiro 12, 2024
                createDateTime(2024, 1, 16)); // Janeiro 16, 2024
        voyageRepository.store(voyage2);

        // Voyage de Singapore para Tokyo
        Voyage voyage3 = createVoyage("V0300", "SGSIN", "JPTYO",
                createDateTime(2024, 1, 20),  // Janeiro 20, 2024
                createDateTime(2024, 1, 24)); // Janeiro 24, 2024
        voyageRepository.store(voyage3);

        // Outras rotas comuns
        Voyage voyage4 = createVoyage("V0400", "USNYC", "GBLON",
                createDateTime(2024, 1, 5),   // Janeiro 5, 2024
                createDateTime(2024, 1, 6));  // Janeiro 6, 2024
        voyageRepository.store(voyage4);

        Voyage voyage5 = createVoyage("V0500", "CNHKG", "JPTYO",
                createDateTime(2024, 1, 8),   // Janeiro 8, 2024
                createDateTime(2024, 1, 10)); // Janeiro 10, 2024
        voyageRepository.store(voyage5);
    }

    private Voyage createVoyage(String voyageNumber, String from, String to, 
                               LocalDateTime departure, LocalDateTime arrival) {
        List<CarrierMovement> carrierMovements = new ArrayList<>();
        carrierMovements.add(new CarrierMovement(
                new Location(from),
                new Location(to),
                departure,
                arrival));
        Schedule schedule = new Schedule(carrierMovements);
        return new Voyage(new VoyageNumber(voyageNumber), schedule);
    }

    private LocalDateTime createDateTime(int year, int month, int day) {
        // Mês: 1-12 (janeiro=1, dezembro=12)
        return LocalDateTime.of(year, month, day, 12, 0, 0);
    }
}
