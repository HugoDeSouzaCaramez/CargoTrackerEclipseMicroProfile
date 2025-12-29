package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.readmodels.CargoReadModel;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.specification.CargoSpecification;
import com.practicalddd.cargotracker.bookingms.domain.specification.Specification;
import com.practicalddd.cargotracker.bookingms.domain.specification.Specifications;
import com.practicalddd.cargotracker.bookingms.infrastructure.repositories.jpa.CargoRepositoryImpl;
import com.practicalddd.cargotracker.bookingms.infrastructure.repositories.readmodels.CargoReadModelRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/cargoqueries/advanced")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class CargoAdvancedQueryController {
    
    @Inject
    private CargoRepositoryImpl cargoRepository;
    
    @Inject
    private CargoReadModelRepository cargoReadModelRepository;
    
    @GET
    @Path("/urgent")
    public Response getUrgentCargos() {
        try {
            // Usando Specification Pattern
            Specification spec = CargoSpecification.urgentCargos();
            List cargos = cargoRepository.findAllBySpecification(spec);
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", cargos.size());
            response.put("cargos", cargos);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    @GET
    @Path("/readmodels")
    public Response getCargoReadModels() {
        try {
            List<CargoReadModel> readModels = cargoReadModelRepository.findAll();
            return Response.ok(readModels).build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    @GET
    @Path("/filter")
    public Response filterCargos(
            @QueryParam("origin") String origin,
            @QueryParam("destination") String destination,
            @QueryParam("minAmount") @DefaultValue("0") int minAmount,
            @QueryParam("urgentOnly") @DefaultValue("false") boolean urgentOnly) {
        
        try {
            Specification spec = CargoSpecification.withBookingAmountGreaterThan(minAmount);
            
            if (origin != null) {
                spec = Specifications.and(spec, CargoSpecification.byOrigin(origin));
            }
            
            if (destination != null) {
                spec = Specifications.and(spec, CargoSpecification.byDestination(destination));
            }
            
            if (urgentOnly) {
                spec = Specifications.and(spec, CargoSpecification.urgentCargos());
            }
            
            List cargos = cargoRepository.findAllBySpecification(spec);
            return Response.ok(cargos).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
}
