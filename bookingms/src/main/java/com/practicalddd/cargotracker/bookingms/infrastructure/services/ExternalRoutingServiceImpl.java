package com.practicalddd.cargotracker.bookingms.infrastructure.services;

import com.practicalddd.cargotracker.bookingms.domain.model.entities.Leg;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.RouteSpecification;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Voyage;
import com.practicalddd.cargotracker.bookingms.infrastructure.services.http.ExternalCargoRoutingClient;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.ExternalRoutingService;
import com.practicalddd.cargotracker.bookingms.application.shared.model.TransitEdge;
import com.practicalddd.cargotracker.bookingms.application.shared.model.TransitPath;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ExternalRoutingServiceImpl implements ExternalRoutingService {

    @Inject
    private ExternalCargoRoutingClient externalCargoRoutingClient;

    @Override
    public CargoItinerary fetchRouteForSpecification(RouteSpecification routeSpecification) {
        TransitPath transitPath = externalCargoRoutingClient.findOptimalRoute(
                routeSpecification.getOrigin().getUnLocCode(),
                routeSpecification.getDestination().getUnLocCode(),
                routeSpecification.getArrivalDeadline().toString());

        List<Leg> legs = new ArrayList<>(transitPath.getTransitEdges().size());
        for (TransitEdge edge : transitPath.getTransitEdges()) {
            legs.add(toLeg(edge));
        }

        return new CargoItinerary(legs);
    }

    private Leg toLeg(TransitEdge edge) {
        return new Leg(
                new Voyage(edge.getVoyageNumber()),
                new Location(edge.getFromUnLocode()),
                new Location(edge.getToUnLocode()),
                edge.getFromDate(),
                edge.getToDate());
    }
}
