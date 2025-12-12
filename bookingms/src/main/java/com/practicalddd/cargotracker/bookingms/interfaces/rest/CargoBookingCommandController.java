package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoBookingCommandPort;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.BookCargoResource;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.ErrorResponse; // Import correto
import com.practicalddd.cargotracker.bookingms.interfaces.rest.transform.BookCargoCommandDTOAssembler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Controlador REST para comandos de booking de cargas.
 * Apenas operações de escrita (HTTP POST).
 */
@Path("/cargobooking/commands")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CargoBookingCommandController {

    private static final Logger logger = Logger.getLogger(CargoBookingCommandController.class.getName());
    
    private final CargoBookingCommandPort cargoBookingCommandPort;

    @Inject
    public CargoBookingCommandController(CargoBookingCommandPort cargoBookingCommandPort) {
        this.cargoBookingCommandPort = cargoBookingCommandPort;
    }

    @POST
    @Path("/book")
    public Response bookCargo(BookCargoResource bookCargoResource) {
        try {
            // Validação básica do DTO
            validateBookCargoResource(bookCargoResource);
            
            BookingId bookingId = cargoBookingCommandPort.bookCargo(
                    BookCargoCommandDTOAssembler.toCommandFromDTO(bookCargoResource));

            return Response.ok()
                    .entity(bookingId)
                    .build();

        } catch (IllegalArgumentException e) {
            // Erros de validação de negócio
            return handleBusinessValidationError(e);
            
        } catch (ConstraintViolationException e) {
            // Erros de validação JSR-380 (Bean Validation)
            return handleConstraintViolationError(e);
            
        } catch (Exception e) {
            // Erro genérico
            logger.severe("Unexpected error in bookCargo: " + e.getMessage());
            e.printStackTrace();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("An unexpected error occurred"))
                    .build();
        }
    }
    
    /**
     * Validação básica do resource
     */
    private void validateBookCargoResource(BookCargoResource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        
        if (resource.getOriginLocation() == null || resource.getOriginLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Origin location is required");
        }
        
        if (resource.getDestLocation() == null || resource.getDestLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination location is required");
        }
        
        if (resource.getDestArrivalDeadline() == null) {
            throw new IllegalArgumentException("Arrival deadline is required");
        }
    }
    
    /**
     * Trata erros de validação de negócio
     */
    private Response handleBusinessValidationError(IllegalArgumentException e) {
        String message = e.getMessage();
        Response.Status status;
        
        if (message.contains("not supported") || 
            message.contains("Booking amount") || 
            message.contains("cannot be null") || 
            message.contains("must be") ||
            message.contains("is required")) {
            
            status = Response.Status.BAD_REQUEST; // 400
            
        } else if (message.contains("deadline") || 
                   message.contains("Arrival deadline") ||
                   message.contains("too soon")) {
            
            // UNPROCESSABLE_ENTITY = 422
            // Usar o valor numérico diretamente
            return Response.status(422)
                    .entity(ErrorResponse.validationError(message, null))
                    .build();
            
        } else if (message.contains("duplicate") || 
                   message.contains("conflict") ||
                   message.contains("already exists")) {
            
            status = Response.Status.CONFLICT; // 409
            
        } else {
            status = Response.Status.BAD_REQUEST; // 400 padrão
        }
        
        return Response.status(status)
                .entity(ErrorResponse.builder()
                    .title(status.getReasonPhrase())
                    .detail(message)
                    .status(status.getStatusCode())
                    .build())
                .build();
    }
    
    /**
     * Trata erros de validação de constraints
     */
    private Response handleConstraintViolationError(ConstraintViolationException e) {
        StringBuilder details = new StringBuilder();
        e.getConstraintViolations().forEach(violation -> {
            details.append(violation.getPropertyPath())
                   .append(": ")
                   .append(violation.getMessage())
                   .append("; ");
        });
        
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse.badRequest(details.toString()))
                .build();
    }
}
