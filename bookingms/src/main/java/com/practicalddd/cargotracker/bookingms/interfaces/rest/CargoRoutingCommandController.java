package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoRoutingCommandPort;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.RouteCargoResource;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.transform.RouteCargoCommandDTOAssembler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controlador REST para comandos de roteamento de cargas.
 * Apenas operações de escrita (HTTP POST).
 */
@Path("/cargorouting/commands")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CargoRoutingCommandController {

    private final CargoRoutingCommandPort cargoRoutingCommandPort;

    @Inject
    public CargoRoutingCommandController(CargoRoutingCommandPort cargoRoutingCommandPort) {
        this.cargoRoutingCommandPort = cargoRoutingCommandPort;
    }

    @POST
    @Path("/route")
    public Response routeCargo(RouteCargoResource routeCargoResource) {
        System.out.println("***Booking Id is***" + routeCargoResource.getBookingId());
        cargoRoutingCommandPort.assignRouteToCargo(
                RouteCargoCommandDTOAssembler.toCommandFromDTO(routeCargoResource));

        return Response.ok()
                .entity("Cargo Routed")
                .build();
    }
}
