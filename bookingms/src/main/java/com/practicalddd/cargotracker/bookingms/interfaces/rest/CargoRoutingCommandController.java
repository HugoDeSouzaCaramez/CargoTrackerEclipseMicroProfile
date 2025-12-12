package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoRoutingCommandPort;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.ErrorResponse;
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
        try {
            System.out.println("***Booking Id received***" + routeCargoResource.getBookingId());

            validateRouteCargoResource(routeCargoResource);

            cargoRoutingCommandPort.assignRouteToCargo(
                    RouteCargoCommandDTOAssembler.toCommandFromDTO(routeCargoResource));

            return Response.ok()
                    .entity("Cargo Routed")
                    .build();

        } catch (IllegalArgumentException e) {
            // Erros de validação
            String message = e.getMessage();

            if (message.contains("Booking ID") ||
                    message.contains("cannot be null") ||
                    message.contains("is required") ||
                    message.contains("only contain")) {

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.badRequest(message))
                        .build();

            } else if (message.contains("deadline") ||
                    message.contains("Routing deadline")) {

                return Response.status(422) // UNPROCESSABLE_ENTITY
                        .entity(ErrorResponse.validationError(message, null))
                        .build();

            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.badRequest(message))
                        .build();
            }

        } catch (Exception e) {
            // logger.severe("Unexpected error in routeCargo: " + e.getMessage());
            e.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("An unexpected error occurred"))
                    .build();
        }
    }

    // Método de validação atualizado:
    private void validateRouteCargoResource(RouteCargoResource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        if (resource.getBookingId() == null || resource.getBookingId().trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID is required");
        }

        // Validação mais simples - apenas tamanho mínimo
        if (resource.getBookingId().length() < 4) {
            throw new IllegalArgumentException("Booking ID must be at least 4 characters");
        }

        // Verificar se contém apenas caracteres válidos
        if (!resource.getBookingId().matches("^[A-Z0-9]+$")) {
            throw new IllegalArgumentException("Booking ID can only contain uppercase letters and numbers");
        }
    }
}
