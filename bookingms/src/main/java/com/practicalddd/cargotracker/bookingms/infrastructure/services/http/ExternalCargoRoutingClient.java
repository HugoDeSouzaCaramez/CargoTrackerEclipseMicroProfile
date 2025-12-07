package com.practicalddd.cargotracker.bookingms.infrastructure.services.http;

import javax.ws.rs.*;

import com.practicalddd.cargotracker.bookingms.infrastructure.services.http.dto.TransitPathDTO;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri="http://routingms:8081/cargoRouting")
public interface ExternalCargoRoutingClient {
    @GET
    @Path("/optimalRoute")
    @Produces({"application/json"})
    TransitPathDTO findOptimalRoute(
            @QueryParam("origin") String originUnLocode,
            @QueryParam("destination") String destinationUnLocode,
            @QueryParam("deadline") String deadline);
}
