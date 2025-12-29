package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.services.PortCommandService;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.commands.CreatePortCommand;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.commands.UpdatePortCapacityCommand;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.commands.RecordPortCargoMovementCommand;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortId;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.CreatePortResource;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.UpdatePortCapacityResource;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.RecordPortCargoMovementResource;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.ErrorResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/port/commands")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PortCommandController {

    private static final Logger logger = Logger.getLogger(PortCommandController.class.getName());

    @Inject
    private PortCommandService portCommandService;

    @POST
    @Path("/create")
    public Response createPort(CreatePortResource createPortResource) {
        try {
            validateCreatePortResource(createPortResource);

            CreatePortCommand command = new CreatePortCommand(
                    createPortResource.getUnLocCode(),
                    createPortResource.getName(),
                    createPortResource.getCountry(),
                    createPortResource.getTimeZone(),
                    createPortResource.getInitialCapacity());

            PortId portId = portCommandService.createPort(command);

            return Response.ok()
                    .entity(portId)
                    .build();

        } catch (IllegalArgumentException e) {
            return handleBusinessValidationError(e);
        } catch (Exception e) {
            logger.severe("Unexpected error in createPort: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to create port"))
                    .build();
        }
    }

    @PUT
    @Path("/updateCapacity")
    public Response updatePortCapacity(UpdatePortCapacityResource updatePortCapacityResource) {
        try {
            validateUpdatePortCapacityResource(updatePortCapacityResource);

            UpdatePortCapacityCommand command = new UpdatePortCapacityCommand(
                    updatePortCapacityResource.getPortUnLocCode(),
                    updatePortCapacityResource.getNewMaxCapacity());

            portCommandService.updatePortCapacity(command);

            return Response.ok()
                    .entity("Port capacity updated successfully")
                    .build();

        } catch (IllegalArgumentException e) {
            return handleBusinessValidationError(e);
        } catch (Exception e) {
            logger.severe("Unexpected error in updatePortCapacity: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to update port capacity"))
                    .build();
        }
    }

    @POST
    @Path("/recordMovement")
    public Response recordCargoMovement(RecordPortCargoMovementResource recordPortCargoMovementResource) {
        try {
            validateRecordPortCargoMovementResource(recordPortCargoMovementResource);

            RecordPortCargoMovementCommand command = new RecordPortCargoMovementCommand(
                    recordPortCargoMovementResource.getPortUnLocCode(),
                    recordPortCargoMovementResource.getCargoAmount(),
                    RecordPortCargoMovementCommand.MovementType.valueOf(
                            recordPortCargoMovementResource.getMovementType().toUpperCase()));

            portCommandService.recordCargoMovement(command);

            return Response.ok()
                    .entity("Cargo movement recorded successfully")
                    .build();

        } catch (IllegalArgumentException e) {
            return handleBusinessValidationError(e);
        } catch (Exception e) {
            logger.severe("Unexpected error in recordCargoMovement: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to record cargo movement"))
                    .build();
        }
    }

    
    @POST
    @Path("/createBatch")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPortsBatch(List<CreatePortResource> portResources) {
        List<PortId> results = new ArrayList<>();
        for (CreatePortResource resource : portResources) {
            try {
                PortId portId = portCommandService.createPort(new CreatePortCommand(
                        resource.getUnLocCode(),
                        resource.getName(),
                        resource.getCountry(),
                        resource.getTimeZone(),
                        resource.getInitialCapacity()));
                results.add(portId);
            } catch (Exception e) {
                // Log e continuar com os outros
            }
        }
        return Response.ok(results).build();
    }

    private void validateCreatePortResource(CreatePortResource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (resource.getUnLocCode() == null || resource.getUnLocCode().trim().isEmpty()) {
            throw new IllegalArgumentException("UN/LOCODE is required");
        }
        if (resource.getName() == null || resource.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Port name is required");
        }
        if (resource.getCountry() == null || resource.getCountry().trim().isEmpty()) {
            throw new IllegalArgumentException("Country is required");
        }
        if (resource.getInitialCapacity() <= 0) {
            throw new IllegalArgumentException("Initial capacity must be positive");
        }
    }

    private void validateUpdatePortCapacityResource(UpdatePortCapacityResource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (resource.getPortUnLocCode() == null || resource.getPortUnLocCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Port UN/LOCODE is required");
        }
        if (resource.getNewMaxCapacity() <= 0) {
            throw new IllegalArgumentException("New max capacity must be positive");
        }
    }

    private void validateRecordPortCargoMovementResource(RecordPortCargoMovementResource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (resource.getPortUnLocCode() == null || resource.getPortUnLocCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Port UN/LOCODE is required");
        }
        if (resource.getCargoAmount() <= 0) {
            throw new IllegalArgumentException("Cargo amount must be positive");
        }
        if (resource.getMovementType() == null || resource.getMovementType().trim().isEmpty()) {
            throw new IllegalArgumentException("Movement type is required (ARRIVAL or DEPARTURE)");
        }
        if (!resource.getMovementType().equalsIgnoreCase("ARRIVAL") &&
                !resource.getMovementType().equalsIgnoreCase("DEPARTURE")) {
            throw new IllegalArgumentException("Movement type must be ARRIVAL or DEPARTURE");
        }
    }

    private Response handleBusinessValidationError(IllegalArgumentException e) {
        String message = e.getMessage();
        Response.Status status;

        if (message.contains("already exists") || message.contains("duplicate")) {
            status = Response.Status.CONFLICT; // 409
        } else if (message.contains("not found") || message.contains("does not exist")) {
            status = Response.Status.NOT_FOUND; // 404
        } else if (message.contains("cannot be") || message.contains("must be") ||
                message.contains("is required") || message.contains("invalid")) {
            status = Response.Status.BAD_REQUEST; // 400
        } else {
            status = Response.Status.BAD_REQUEST; // default
        }

        return Response.status(status)
                .entity(ErrorResponse.builder()
                        .title(status.getReasonPhrase())
                        .detail(message)
                        .status(status.getStatusCode())
                        .build())
                .build();
    }
}
