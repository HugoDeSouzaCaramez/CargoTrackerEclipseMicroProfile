package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoBookingInboundPort;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.BookCargoResource;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.transform.BookCargoCommandDTOAssembler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/cargobooking")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CargoBookingController {

    private final CargoBookingInboundPort cargoBookingInboundPort;

    @Inject
    public CargoBookingController(CargoBookingInboundPort cargoBookingInboundPort){
        this.cargoBookingInboundPort = cargoBookingInboundPort;
    }

    @POST
    public Response bookCargo(BookCargoResource bookCargoResource){
        BookingId bookingId  = cargoBookingInboundPort.bookCargo(
                BookCargoCommandDTOAssembler.toCommandFromDTO(bookCargoResource));

        return Response.ok()
                .entity(bookingId)
                .build();
    }
}
