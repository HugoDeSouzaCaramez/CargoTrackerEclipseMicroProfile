package com.practicalddd.cargotracker.routingms.application.internal.seeders;

import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.routingms.domain.model.entities.CarrierMovement;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Schedule;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.VoyageNumber;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.routingms.infrastructure.repositories.jpa.VoyageRepository;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@ApplicationScoped
public class VoyageDataSeeder {

    @Inject
    private VoyageRepository voyageRepository;

    @PostConstruct
    @Transactional
    public void initializeSampleData() {
        System.out.println("=== INITIALIZING VOYAGE SAMPLE DATA ===");
        
        if (voyageRepository.findAll().isEmpty()) {
            createSampleVoyages();
            System.out.println("=== SAMPLE VOYAGES CREATED SUCCESSFULLY ===");
        } else {
            System.out.println("=== VOYAGES ALREADY EXIST, SKIPPING SEEDING ===");
        }
    }

    private void createSampleVoyages() {
        List<Voyage> sampleVoyages = new ArrayList<>();

        // Voyage 1: Hong Kong to New York
        Voyage voyage1 = new Voyage(
            new VoyageNumber("V001"),
            new Schedule(Arrays.asList(
                new CarrierMovement(
                    new Location("CNHKG"),
                    new Location("USNYC"),
                    createDateTime(2024, 10, 1),  // Outubro 1, 2024
                    createDateTime(2024, 10, 20) // Outubro 20, 2024
                )
            ))
        );
        sampleVoyages.add(voyage1);

        // Voyage 2: Shanghai to Los Angeles
        Voyage voyage2 = new Voyage(
            new VoyageNumber("V002"),
            new Schedule(Arrays.asList(
                new CarrierMovement(
                    new Location("CNSHA"),
                    new Location("USLAX"),
                    createDateTime(2024, 10, 5),  // Outubro 5, 2024
                    createDateTime(2024, 10, 22) // Outubro 22, 2024
                )
            ))
        );
        sampleVoyages.add(voyage2);

        // Voyage 3: Singapore to Rotterdam
        Voyage voyage3 = new Voyage(
            new VoyageNumber("V003"),
            new Schedule(Arrays.asList(
                new CarrierMovement(
                    new Location("SGSIN"),
                    new Location("NLRTM"),
                    createDateTime(2024, 10, 10), // Outubro 10, 2024
                    createDateTime(2024, 10, 25) // Outubro 25, 2024
                )
            ))
        );
        sampleVoyages.add(voyage3);

        // Voyage 4: Multi-leg voyage
        Voyage voyage4 = new Voyage(
            new VoyageNumber("V004"),
            new Schedule(Arrays.asList(
                new CarrierMovement(
                    new Location("CNHKG"),
                    new Location("SGSIN"),
                    createDateTime(2024, 10, 2),  // Outubro 2, 2024
                    createDateTime(2024, 10, 5)   // Outubro 5, 2024
                ),
                new CarrierMovement(
                    new Location("SGSIN"),
                    new Location("NLRTM"),
                    createDateTime(2024, 10, 6),  // Outubro 6, 2024
                    createDateTime(2024, 10, 20)  // Outubro 20, 2024
                ),
                new CarrierMovement(
                    new Location("NLRTM"),
                    new Location("USNYC"),
                    createDateTime(2024, 10, 21), // Outubro 21, 2024
                    createDateTime(2024, 10, 28)  // Outubro 28, 2024
                )
            ))
        );
        sampleVoyages.add(voyage4);

        // Persist all voyages
        for (Voyage voyage : sampleVoyages) {
            voyageRepository.store(voyage);
            System.out.println("Created voyage: " + voyage.getVoyageNumber().getVoyageNumber());
        }
    }

    private LocalDateTime createDateTime(int year, int month, int day) {
        return LocalDateTime.of(year, month, day, 12, 0, 0);
    }
    
    private LocalDateTime createDateTime(int year, Month month, int day) {
        return LocalDateTime.of(year, month, day, 12, 0, 0);
    }
}
