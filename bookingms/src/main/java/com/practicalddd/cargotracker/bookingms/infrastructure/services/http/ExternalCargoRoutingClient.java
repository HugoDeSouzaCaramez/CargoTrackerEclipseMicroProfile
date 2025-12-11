package com.practicalddd.cargotracker.bookingms.infrastructure.services.http;

import javax.ws.rs.*;
import com.practicalddd.cargotracker.bookingms.infrastructure.services.http.dto.TransitPathDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import javax.enterprise.context.ApplicationScoped;

@RegisterRestClient(baseUri = "http://routingms:8081/cargoRouting")
@ApplicationScoped
public interface ExternalCargoRoutingClient {
    
    @GET
    @Path("/optimalRoute")
    @Produces({"application/json"})
    TransitPathDTO findOptimalRoute(
        @QueryParam("origin") String originUnLocode,
        @QueryParam("destination") String destinationUnLocode,
        @QueryParam("deadline") String deadline
    );
    
    // Endpoint de health check
    @GET
    @Path("/health")
    @Produces({"application/json"})
    String healthCheck();
}
