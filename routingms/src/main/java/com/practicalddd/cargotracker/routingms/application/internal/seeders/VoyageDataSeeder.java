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
import java.util.Date;
import java.util.List;
import java.util.Calendar;

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
                    createDate(2024, Calendar.OCTOBER, 1),
                    createDate(2024, Calendar.OCTOBER, 20)
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
                    createDate(2024, Calendar.OCTOBER, 5),
                    createDate(2024, Calendar.OCTOBER, 22)
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
                    createDate(2024, Calendar.OCTOBER, 10),
                    createDate(2024, Calendar.OCTOBER, 25)
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
                    createDate(2024, Calendar.OCTOBER, 2),
                    createDate(2024, Calendar.OCTOBER, 5)
                ),
                new CarrierMovement(
                    new Location("SGSIN"),
                    new Location("NLRTM"),
                    createDate(2024, Calendar.OCTOBER, 6),
                    createDate(2024, Calendar.OCTOBER, 20)
                ),
                new CarrierMovement(
                    new Location("NLRTM"),
                    new Location("USNYC"),
                    createDate(2024, Calendar.OCTOBER, 21),
                    createDate(2024, Calendar.OCTOBER, 28)
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

    private Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 12, 0, 0);
        return calendar.getTime();
    }
}