package com.practicalddd.cargotracker.routingms.interfaces.rest;

import com.practicalddd.cargotracker.routingms.application.internal.queryservices.CargoRoutingQueryService;
import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.routingms.domain.model.entities.CarrierMovement;
import com.practicalddd.cargotracker.shareddomain.model.TransitEdge;
import com.practicalddd.cargotracker.shareddomain.model.TransitPath;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

@Path("/cargoRouting")
@ApplicationScoped
public class CargoRoutingController {

    private static final Logger logger = Logger.getLogger(CargoRoutingController.class.getName());

    @Inject
    private CargoRoutingQueryService cargoRoutingQueryService;

    @GET
    @Path("/optimalRoute")
    @Produces({ "application/json" })
    public TransitPath findOptimalRoute(
            @QueryParam("origin") String originUnLocode,
            @QueryParam("destination") String destinationUnLocode,
            @QueryParam("deadline") String deadline) {

        logger.info("=== SEARCHING ROUTE ===");
        logger.info("Origin: " + originUnLocode);
        logger.info("Destination: " + destinationUnLocode);
        logger.info("Deadline: " + deadline);

        List<Voyage> voyages = cargoRoutingQueryService.findAll();
        TransitPath transitPath = new TransitPath();
        List<TransitEdge> transitEdges = new ArrayList<>();

        // Converter deadline string para Date
        Date deadlineDate = parseDeadline(deadline);

        for (Voyage voyage : voyages) {
            List<CarrierMovement> movements = voyage.getSchedule().getCarrierMovements();

            if (!movements.isEmpty()) {
                CarrierMovement movement = movements.get(0);

                // Verificar se o movimento conecta origem e destino
                if (matchesRoute(movement, originUnLocode, destinationUnLocode, deadlineDate)) {
                    TransitEdge transitEdge = new TransitEdge();
                    transitEdge.setVoyageNumber(voyage.getVoyageNumber().getVoyageNumber());
                    transitEdge.setFromDate(movement.getDepartureDate());
                    transitEdge.setToDate(movement.getArrivalDate());
                    transitEdge.setFromUnLocode(movement.getDepartureLocation().getUnLocCode());
                    transitEdge.setToUnLocode(movement.getArrivalLocation().getUnLocCode());
                    transitEdges.add(transitEdge);

                    logger.info("Found matching voyage: " + voyage.getVoyageNumber().getVoyageNumber());
                }
            }
        }

        transitPath.setTransitEdges(transitEdges);
        logger.info("=== ROUTE SEARCH COMPLETE - " + transitEdges.size() + " EDGES FOUND ===");

        return transitPath;
    }

    private boolean matchesRoute(CarrierMovement movement, String origin, String destination, Date deadline) {
        if (movement == null) {
            logger.warning("❌ Movement is null");
            return false;
        }

        String departureLoc = movement.getDepartureLocation() != null ? movement.getDepartureLocation().getUnLocCode()
                : "NULL";
        String arrivalLoc = movement.getArrivalLocation() != null ? movement.getArrivalLocation().getUnLocCode()
                : "NULL";
        Date arrivalDate = movement.getArrivalDate();

        logger.info("🔍 Checking movement: " + departureLoc + " → " + arrivalLoc +
                " (Arrival: " + arrivalDate + ")");
        logger.info("🎯 Target: " + origin + " → " + destination + " (Deadline: " + deadline + ")");

        // Verificar se os dados do movement são válidos
        if (movement.getDepartureLocation() == null || movement.getArrivalLocation() == null) {
            logger.warning("❌ Movement has null locations");
            return false;
        }

        if (arrivalDate == null) {
            logger.warning("❌ Movement has null arrival date");
            return false;
        }

        boolean locationMatches = departureLoc.equals(origin) && arrivalLoc.equals(destination);
        boolean timeMatches = arrivalDate.before(deadline) || arrivalDate.equals(deadline);
        boolean matches = locationMatches && timeMatches;

        logger.info("📋 Match result - Locations: " + locationMatches +
                ", Time: " + timeMatches + " → Overall: " + matches);

        return matches;
    }

    private Date parseDeadline(String deadline) {
        try {
            // Tenta parse no formato enviado pelo BookingMS
            return new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH).parse(deadline);
        } catch (ParseException e1) {
            try {
                // Fallback para formato ISO
                return new SimpleDateFormat("yyyy-MM-dd").parse(deadline);
            } catch (ParseException e2) {
                logger.warning("Failed to parse deadline: " + deadline);
                // Log do erro específico
                logger.warning("Parse error: " + e1.getMessage());
                // Retorna uma data futura padrão se não conseguir parse
                return new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000); // 30 dias no futuro
            }
        }
    }

    @GET
    @Path("/debug/voyages")
    @Produces({ "application/json" })
    public List<Voyage> debugVoyages() {
        logger.info("🔧 === DEBUG: Listing all voyages ===");
        List<Voyage> voyages = cargoRoutingQueryService.findAll();
        logger.info("🔧 Found " + voyages.size() + " voyages");

        for (Voyage voyage : voyages) {
            List<CarrierMovement> movements = voyage.getSchedule().getCarrierMovements();
            logger.info("🔧 Voyage " + voyage.getVoyageNumber().getVoyageNumber() +
                    " has " + movements.size() + " movements");

            for (CarrierMovement movement : movements) {
                logger.info("🔧   Movement: " +
                        movement.getDepartureLocation().getUnLocCode() + " → " +
                        movement.getArrivalLocation().getUnLocCode() + " (" +
                        movement.getDepartureDate() + " to " + movement.getArrivalDate() + ")");
            }
        }

        return voyages;
    }
}