package com.practicalddd.cargotracker.bookingms.infrastructure.services;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.entities.Leg;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.CargoItinerary;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.Location;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.RouteSpecification;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.Voyage;
import com.practicalddd.cargotracker.bookingms.infrastructure.services.http.ExternalCargoRoutingClient;
import com.practicalddd.cargotracker.bookingms.infrastructure.services.http.dto.TransitEdgeDTO;
import com.practicalddd.cargotracker.bookingms.infrastructure.services.http.dto.TransitPathDTO;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.ExternalRoutingService;
import com.practicalddd.cargotracker.bookingms.infrastructure.services.http.ResilientHttpClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class ExternalRoutingServiceImpl implements ExternalRoutingService {
    
    private static final Logger logger = Logger.getLogger(ExternalRoutingServiceImpl.class.getName());
    
    @Inject
    private ExternalCargoRoutingClient externalCargoRoutingClient;
    
    @Inject
    private ResilientHttpClient resilientHttpClient;
    
    @Override
    public CargoItinerary fetchRouteForSpecification(RouteSpecification routeSpecification) {
        String deadlineString = routeSpecification.getArrivalDeadline().toString();
        
        try {
            // Usando resiliência para chamadas externas
            TransitPathDTO transitPath = resilientHttpClient.executeWithRetry(
                () -> externalCargoRoutingClient.findOptimalRoute(
                    routeSpecification.getOrigin().getUnLocCode(),
                    routeSpecification.getDestination().getUnLocCode(),
                    deadlineString
                ),
                "fetchOptimalRoute"
            );
            
            if (transitPath == null || transitPath.getTransitEdges() == null) {
                logger.warning("No route found or empty response from routing service");
                return new CargoItinerary(new ArrayList<>());
            }
            
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
            
        } catch (Exception e) {
            logger.severe("Failed to fetch route after retries: " + e.getMessage());
            // Retornar itinerário vazio como fallback
            return new CargoItinerary(new ArrayList<>());
        }
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
