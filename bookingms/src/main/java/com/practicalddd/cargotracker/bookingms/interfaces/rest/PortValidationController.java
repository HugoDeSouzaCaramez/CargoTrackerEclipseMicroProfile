package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.services.PortCommandService;
import com.practicalddd.cargotracker.bookingms.domain.services.CargoPortValidationService;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.ErrorResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/validation")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class PortValidationController {

    private static final Logger logger = Logger.getLogger(PortValidationController.class.getName());
    
    @Inject
    private CargoPortValidationService cargoPortValidationService;
    
    @Inject
    private PortCommandService portCommandService;

    @GET
    @Path("/ports/feasibility")
    public Response validateBookingFeasibility(
            @QueryParam("origin") String originUnLocCode,
            @QueryParam("destination") String destinationUnLocCode,
            @QueryParam("amount") @DefaultValue("1") int cargoAmount) {
        
        try {
            if (originUnLocCode == null || destinationUnLocCode == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.badRequest("Origin and destination parameters are required"))
                        .build();
            }
            
            CargoPortValidationService.ValidationResult result = 
                cargoPortValidationService.validateBookingFeasibility(
                    originUnLocCode, destinationUnLocCode, cargoAmount
                );
            
            return Response.ok(new ValidationResponse(result)).build();
            
        } catch (Exception e) {
            logger.severe("Error validating booking feasibility: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Validation error"))
                    .build();
        }
    }
    
    @GET
    @Path("/ports/capacity")
    public Response validatePortCapacity(
            @QueryParam("port") String portUnLocCode,
            @QueryParam("amount") @DefaultValue("1") int cargoAmount) {
        
        try {
            if (portUnLocCode == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.badRequest("Port parameter is required"))
                        .build();
            }
            
            CargoPortValidationService.ValidationResult result = 
                cargoPortValidationService.validatePortCapacity(portUnLocCode, cargoAmount);
            
            return Response.ok(new ValidationResponse(result)).build();
            
        } catch (Exception e) {
            logger.severe("Error validating port capacity: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Validation error"))
                    .build();
        }
    }
    
    @GET
    @Path("/ports/usage")
    public Response getPortUsage(@QueryParam("port") String portUnLocCode) {
        try {
            if (portUnLocCode == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.badRequest("Port parameter is required"))
                        .build();
            }
            
            double usagePercentage = portCommandService.getPortUsagePercentage(portUnLocCode);
            boolean canAccommodate = portCommandService.canPortAccommodateCargo(portUnLocCode, 1); // Teste mínimo
            
            return Response.ok(new PortUsageResponse(portUnLocCode, usagePercentage, canAccommodate)).build();
            
        } catch (Exception e) {
            logger.severe("Error getting port usage: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Error getting port usage"))
                    .build();
        }
    }
    
    // DTOs de resposta
    public static class ValidationResponse {
        private boolean valid;
        private String errors;
        private String warnings;
        private String portStatus;
        
        public ValidationResponse(CargoPortValidationService.ValidationResult result) {
            this.valid = result.isValid();
            this.errors = result.getErrorSummary();
            this.warnings = result.getWarningSummary();
        }
        
        // Getters e Setters
        public boolean isValid() { return valid; }
        public String getErrors() { return errors; }
        public String getWarnings() { return warnings; }
        public String getPortStatus() { return portStatus; }
    }
    
    public static class PortUsageResponse {
        private String portUnLocCode;
        private double usagePercentage;
        private boolean canAccommodateCargo;
        private String status;
        
        public PortUsageResponse(String portUnLocCode, double usagePercentage, boolean canAccommodateCargo) {
            this.portUnLocCode = portUnLocCode;
            this.usagePercentage = usagePercentage;
            this.canAccommodateCargo = canAccommodateCargo;
            this.status = usagePercentage > 80 ? "CONGESTED" : 
                         usagePercentage > 60 ? "BUSY" : "OPERATIONAL";
        }
        
        // Getters e Setters
        public String getPortUnLocCode() { return portUnLocCode; }
        public double getUsagePercentage() { return usagePercentage; }
        public boolean isCanAccommodateCargo() { return canAccommodateCargo; }
        public String getStatus() { return status; }
    }
}
