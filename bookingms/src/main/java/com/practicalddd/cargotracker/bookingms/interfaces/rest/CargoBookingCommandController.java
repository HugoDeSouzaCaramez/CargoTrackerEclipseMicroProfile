package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoBookingCommandPort;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.BookCargoResource;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.transform.BookCargoCommandDTOAssembler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controlador REST para comandos de booking de cargas.
 * Apenas operações de escrita (HTTP POST).
 */
@Path("/cargobooking/commands")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CargoBookingCommandController {

    private final CargoBookingCommandPort cargoBookingCommandPort;

    @Inject
    public CargoBookingCommandController(CargoBookingCommandPort cargoBookingCommandPort){
        this.cargoBookingCommandPort = cargoBookingCommandPort;
    }

    @POST
    @Path("/book")
    public Response bookCargo(BookCargoResource bookCargoResource){
        BookingId bookingId = cargoBookingCommandPort.bookCargo(
                BookCargoCommandDTOAssembler.toCommandFromDTO(bookCargoResource));

        return Response.ok()
                .entity(bookingId)
                .build();
    }
}
