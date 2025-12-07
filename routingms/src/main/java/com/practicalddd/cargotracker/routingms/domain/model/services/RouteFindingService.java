package com.practicalddd.cargotracker.routingms.domain.model.services;

import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Location;

import java.time.LocalDateTime;
import java.util.List;

public interface RouteFindingService {
    List<Voyage> findAvailableVoyages(Location origin, Location destination, LocalDateTime deadline);
}
