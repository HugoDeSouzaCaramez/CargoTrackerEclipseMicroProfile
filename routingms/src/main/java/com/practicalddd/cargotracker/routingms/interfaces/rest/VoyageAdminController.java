package com.practicalddd.cargotracker.routingms.interfaces.rest;

import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.routingms.domain.model.entities.CarrierMovement;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Schedule;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.VoyageNumber;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.routingms.infrastructure.repositories.jpa.VoyageRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.logging.Logger;

@Path("/voyageAdmin")
@ApplicationScoped
public class VoyageAdminController {

    private static final Logger logger = Logger.getLogger(VoyageAdminController.class.getName());

    @Inject
    private VoyageRepository voyageRepository;

    @POST
    @Path("/createVoyage")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createVoyage(CreateVoyageRequest request) {
        try {
            logger.info("Creating voyage: " + request.voyageNumber);
            
            Voyage voyage = new Voyage(
                new VoyageNumber(request.voyageNumber),
                new Schedule(Arrays.asList(
                    new CarrierMovement(
                        new Location(request.departureLocation),
                        new Location(request.arrivalLocation),
                        request.departureDate,
                        request.arrivalDate
                    )
                ))
            );
            
            voyageRepository.store(voyage);
            
            logger.info("Voyage created successfully: " + request.voyageNumber);
            return Response.ok().entity("Voyage created successfully").build();
            
        } catch (Exception e) {
            logger.severe("Error creating voyage: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating voyage: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/voyages")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllVoyages() {
        try {
            List<Voyage> voyages = voyageRepository.findAll();
            logger.info("Retrieved " + voyages.size() + " voyages");
            return Response.ok().entity(voyages).build();
        } catch (Exception e) {
            logger.severe("Error retrieving voyages: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving voyages: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/initializeSampleData")
    @Produces(MediaType.APPLICATION_JSON)
    public Response initializeSampleData() {
        try {
            if (voyageRepository.findAll().isEmpty()) {
                // Create sample voyages
                createSampleVoyages();
                logger.info("Sample data initialized successfully");
                return Response.ok().entity("Sample data initialized successfully").build();
            } else {
                logger.info("Voyages already exist, skipping initialization");
                return Response.ok().entity("Voyages already exist").build();
            }
        } catch (Exception e) {
            logger.severe("Error initializing sample data: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error initializing sample data: " + e.getMessage())
                    .build();
        }
    }

    private void createSampleVoyages() {}

    public static class CreateVoyageRequest {
        public String voyageNumber;
        public String departureLocation;
        public String arrivalLocation;
        public Date departureDate;
        public Date arrivalDate;
    }
}