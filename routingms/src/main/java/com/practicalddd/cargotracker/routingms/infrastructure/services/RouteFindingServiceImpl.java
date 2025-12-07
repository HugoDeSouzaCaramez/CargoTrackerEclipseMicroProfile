package com.practicalddd.cargotracker.routingms.infrastructure.services;

import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.routingms.domain.model.entities.CarrierMovement;
import com.practicalddd.cargotracker.routingms.domain.model.repositories.VoyageRepository;
import com.practicalddd.cargotracker.routingms.domain.model.services.RouteFindingService;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Location;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RouteFindingServiceImpl implements RouteFindingService {

    @Inject
    private VoyageRepository voyageRepository;

    @Override
    public List<Voyage> findAvailableVoyages(Location origin, Location destination, LocalDateTime deadline) {
        // Validação de parâmetros
        if (origin == null) {
            throw new IllegalArgumentException("Origin location cannot be null");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination location cannot be null");
        }
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline cannot be null");
        }
        
        List<Voyage> allVoyages = voyageRepository.findAll();
        
        return allVoyages.stream()
                .filter(voyage -> hasMatchingRoute(voyage, origin, destination, deadline))
                .collect(Collectors.toList());
    }

    private boolean hasMatchingRoute(Voyage voyage, Location origin, Location destination, LocalDateTime deadline) {
        if (voyage == null || voyage.getSchedule() == null) {
            return false;
        }
        
        return voyage.getSchedule().getCarrierMovements().stream()
                .anyMatch(movement -> matchesRoute(movement, origin, destination, deadline));
    }

    private boolean matchesRoute(CarrierMovement movement, Location origin, 
                                Location destination, LocalDateTime deadline) {
        if (movement == null || movement.getArrivalDate() == null || movement.getDepartureDate() == null) {
            return false;
        }

        boolean locationMatches = movement.getDepartureLocation().equals(origin) && 
                                movement.getArrivalLocation().equals(destination);
        
        boolean timeMatches = movement.getArrivalDate().isBefore(deadline) || 
                            movement.getArrivalDate().isEqual(deadline);

        return locationMatches && timeMatches;
    }
}
