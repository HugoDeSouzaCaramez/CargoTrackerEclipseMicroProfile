package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.services.PortCommandService;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.ErrorResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/port/queries")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class PortQueryController {

    private static final Logger logger = Logger.getLogger(PortQueryController.class.getName());
    
    @Inject
    private PortCommandService portCommandService;

    @GET
    @Path("/canAccommodate")
    public Response canPortAccommodateCargo(
            @QueryParam("portUnLocCode") String portUnLocCode,
            @QueryParam("cargoAmount") int cargoAmount) {
        try {
            if (portUnLocCode == null || portUnLocCode.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.badRequest("Port UN/LOCODE is required"))
                        .build();
            }
            if (cargoAmount <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.badRequest("Cargo amount must be positive"))
                        .build();
            }
            
            boolean canAccommodate = portCommandService.canPortAccommodateCargo(portUnLocCode, cargoAmount);
            
            return Response.ok()
                    .entity(new AccommodationResponse(canAccommodate))
                    .build();
                    
        } catch (Exception e) {
            logger.severe("Error checking port accommodation: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to check port accommodation"))
                    .build();
        }
    }

    @GET
    @Path("/usagePercentage")
    public Response getPortUsagePercentage(@QueryParam("portUnLocCode") String portUnLocCode) {
        try {
            if (portUnLocCode == null || portUnLocCode.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.badRequest("Port UN/LOCODE is required"))
                        .build();
            }
            
            double usagePercentage = portCommandService.getPortUsagePercentage(portUnLocCode);
            
            return Response.ok()
                    .entity(new UsagePercentageResponse(usagePercentage))
                    .build();
                    
        } catch (Exception e) {
            logger.severe("Error getting port usage percentage: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to get port usage percentage"))
                    .build();
        }
    }
    
    // DTOs de resposta
    public static class AccommodationResponse {
        private boolean canAccommodate;
        
        public AccommodationResponse(boolean canAccommodate) {
            this.canAccommodate = canAccommodate;
        }
        
        public boolean isCanAccommodate() { return canAccommodate; }
        public void setCanAccommodate(boolean canAccommodate) { this.canAccommodate = canAccommodate; }
    }
    
    public static class UsagePercentageResponse {
        private double usagePercentage;
        
        public UsagePercentageResponse(double usagePercentage) {
            this.usagePercentage = usagePercentage;
        }
        
        public double getUsagePercentage() { return usagePercentage; }
        public void setUsagePercentage(double usagePercentage) { this.usagePercentage = usagePercentage; }
    }
}
