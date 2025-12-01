package com.practicalddd.cargotracker.routingms.interfaces.rest;

import com.practicalddd.cargotracker.routingms.application.internal.queryservices.CargoRoutingQueryService;
import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.shareddomain.model.TransitPath;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    @Produces(MediaType.APPLICATION_JSON)
    public TransitPath findOptimalRoute(
            @QueryParam("origin") String originUnLocode,
            @QueryParam("destination") String destinationUnLocode,
            @QueryParam("deadline") String deadline) {

        logger.info("Searching route from " + originUnLocode + " to " + destinationUnLocode);
        
        Date deadlineDate = parseDeadline(deadline);
        return cargoRoutingQueryService.findOptimalRoute(originUnLocode, destinationUnLocode, deadlineDate);
    }

    @GET
    @Path("/voyages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Voyage> getAllVoyages() {
        return cargoRoutingQueryService.findAllVoyages();
    }

    private Date parseDeadline(String deadline) {
        try {
            return new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH).parse(deadline);
        } catch (ParseException e1) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(deadline);
            } catch (ParseException e2) {
                logger.warning("Failed to parse deadline: " + deadline);
                return new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);
            }
        }
    }
}
