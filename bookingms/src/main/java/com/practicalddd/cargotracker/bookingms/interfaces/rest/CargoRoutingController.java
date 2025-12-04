package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoRoutingInboundPort;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.RouteCargoResource;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.transform.RouteCargoCommandDTOAssembler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/cargorouting")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CargoRoutingController {

    private final CargoRoutingInboundPort cargoRoutingInboundPort;

    @Inject
    public CargoRoutingController(CargoRoutingInboundPort cargoRoutingInboundPort) {
        this.cargoRoutingInboundPort = cargoRoutingInboundPort;
    }

    @POST
    public Response routeCargo(RouteCargoResource routeCargoResource) {
        System.out.println("***Booking Id is***" + routeCargoResource.getBookingId());
        cargoRoutingInboundPort.assignRouteToCargo(
                RouteCargoCommandDTOAssembler.toCommandFromDTO(routeCargoResource));

        return Response.ok()
                .entity("Cargo Routed")
                .build();
    }
}
