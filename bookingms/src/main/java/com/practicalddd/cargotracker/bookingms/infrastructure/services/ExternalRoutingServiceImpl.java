package com.practicalddd.cargotracker.bookingms.infrastructure.services;

import com.practicalddd.cargotracker.bookingms.domain.model.entities.Leg;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.RouteSpecification;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Voyage;
import com.practicalddd.cargotracker.bookingms.infrastructure.services.http.ExternalCargoRoutingClient;
import com.practicalddd.cargotracker.bookingms.infrastructure.services.http.dto.TransitEdgeDTO;
import com.practicalddd.cargotracker.bookingms.infrastructure.services.http.dto.TransitPathDTO;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.ExternalRoutingService;

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
        // Converter LocalDateTime para String ISO
        String deadlineString = routeSpecification.getArrivalDeadline().toString();

        TransitPathDTO transitPath = externalCargoRoutingClient.findOptimalRoute(
                routeSpecification.getOrigin().getUnLocCode(),
                routeSpecification.getDestination().getUnLocCode(),
                deadlineString);

        List<Leg> legs = new ArrayList<>(transitPath.getTransitEdges().size());
        for (TransitEdgeDTO edge : transitPath.getTransitEdges()) {
            legs.add(new Leg(
                    new Voyage(edge.getVoyageNumber()),
                    new Location(edge.getFromUnLocode()),
                    new Location(edge.getToUnLocode()),
                    edge.getFromDate(),
                    edge.getToDate()
            ));
        }

        return new CargoItinerary(legs);
    }

    private Leg toLeg(TransitEdgeDTO edge) {
        return new Leg(
                new Voyage(edge.getVoyageNumber()),
                new Location(edge.getFromUnLocode()),
                new Location(edge.getToUnLocode()),
                edge.getFromDate(),
                edge.getToDate());
    }
}
