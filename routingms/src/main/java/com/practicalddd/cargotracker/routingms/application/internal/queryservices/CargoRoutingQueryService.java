package com.practicalddd.cargotracker.routingms.application.internal.queryservices;

import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.routingms.domain.model.entities.CarrierMovement;
import com.practicalddd.cargotracker.routingms.domain.model.repositories.VoyageRepository;
import com.practicalddd.cargotracker.routingms.domain.model.services.RouteFindingService;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.shareddomain.model.TransitEdge;
import com.practicalddd.cargotracker.shareddomain.model.TransitPath;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CargoRoutingQueryService {

    @Inject
    private VoyageRepository voyageRepository;
    
    @Inject
    private RouteFindingService routeFindingService;

    public List<Voyage> findAllVoyages() {
        return voyageRepository.findAll();
    }

    public TransitPath findOptimalRoute(String origin, String destination, LocalDateTime deadline) {
        List<Voyage> availableVoyages = routeFindingService.findAvailableVoyages(
            new Location(origin), 
            new Location(destination), 
            deadline
        );

        List<TransitEdge> transitEdges = availableVoyages.stream()
            .flatMap(voyage -> 
                voyage.getSchedule().getCarrierMovements().stream()
                    .map(movement -> createTransitEdge(voyage, movement))
            )
            .collect(Collectors.toList());

        return new TransitPath(transitEdges);
    }

    private TransitEdge createTransitEdge(Voyage voyage, CarrierMovement movement) {
        return new TransitEdge(
            voyage.getVoyageNumber().getVoyageNumber(),
            movement.getDepartureLocation().getUnLocCode(),
            movement.getArrivalLocation().getUnLocCode(),
            movement.getDepartureDate(),
            movement.getArrivalDate()
        );
    }
}
