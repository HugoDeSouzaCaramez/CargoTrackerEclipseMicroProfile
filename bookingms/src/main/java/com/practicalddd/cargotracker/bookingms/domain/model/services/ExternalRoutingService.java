package com.practicalddd.cargotracker.bookingms.domain.model.services;

import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.RouteSpecification;

public interface ExternalRoutingService {
    CargoItinerary fetchRouteForSpecification(RouteSpecification routeSpecification);
}