package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.commandservices.CargoBulkOperationService;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.BookCargoResource;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.transform.BookCargoCommandDTOAssembler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para comandos de booking em lote.
 */
@Path("/cargobooking/bulk")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CargoBulkCommandController {

    @Inject
    private CargoBulkOperationService cargoBulkOperationService;

    @POST
    @Path("/book")
    public Response bulkBookCargo(List<BookCargoResource> bookCargoResources) {
        try {
            // Converte os recursos em comandos
            List<BookCargoCommand> commands = bookCargoResources.stream()
                    .map(BookCargoCommandDTOAssembler::toCommandFromDTO)
                    .collect(Collectors.toList());

            // Processa em lote
            List<BookingId> bookingIds = cargoBulkOperationService.processBulkBookings(commands);

            return Response.ok(bookingIds).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Bulk booking operation failed: " + e.getMessage())
                    .build();
        }
    }
}
