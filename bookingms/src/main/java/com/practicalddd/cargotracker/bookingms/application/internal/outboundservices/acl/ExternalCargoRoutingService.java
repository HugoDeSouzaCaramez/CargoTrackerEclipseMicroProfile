package com.practicalddd.cargotracker.bookingms.application.internal.outboundservices.acl;

import com.practicalddd.cargotracker.bookingms.domain.model.entities.Leg;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.RouteSpecification;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Voyage;
import com.practicalddd.cargotracker.bookingms.infrastructure.services.http.ExternalCargoRoutingClient;
import com.practicalddd.cargotracker.shareddomain.model.TransitEdge;
import com.practicalddd.cargotracker.shareddomain.model.TransitPath;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class ExternalCargoRoutingService {

    private static final Logger logger = Logger.getLogger(ExternalCargoRoutingService.class.getName());

    @Inject
    @RestClient
    private ExternalCargoRoutingClient externalCargoRoutingClient;

    public CargoItinerary fetchRouteForSpecification(RouteSpecification routeSpecification) {

        System.out.println("=== FETCHING ROUTE FOR SPECIFICATION ===");
        System.out.println("Origin: " + routeSpecification.getOrigin().getUnLocCode());
        System.out.println("Destination: " + routeSpecification.getDestination().getUnLocCode());
        System.out.println("Deadline: " + routeSpecification.getArrivalDeadline());

        TransitPath transitPath = externalCargoRoutingClient.findOptimalRoute(
                routeSpecification.getOrigin().getUnLocCode(),
                routeSpecification.getDestination().getUnLocCode(),
                routeSpecification.getArrivalDeadline().toString());

        System.out.println("Received transit path with " + transitPath.getTransitEdges().size() + " edges");

        List<Leg> legs = new ArrayList<Leg>(transitPath.getTransitEdges().size());
        for (TransitEdge edge : transitPath.getTransitEdges()) {
            legs.add(toLeg(edge));
            System.out.println("Edge: " + edge.getVoyageNumber() + " from " +
                    edge.getFromUnLocode() + " to " + edge.getToUnLocode());
        }

        System.out.println("=== ROUTE FETCHED SUCCESSFULLY - " + legs.size() + " LEGS ===");

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