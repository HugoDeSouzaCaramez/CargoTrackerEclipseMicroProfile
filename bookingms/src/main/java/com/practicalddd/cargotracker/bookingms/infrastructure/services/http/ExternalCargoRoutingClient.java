package com.practicalddd.cargotracker.bookingms.infrastructure.services.http;

import com.practicalddd.cargotracker.bookingms.application.shared.model.TransitPath;
import javax.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri="http://routingms:8081/cargoRouting")
public interface ExternalCargoRoutingClient {
    @GET
    @Path("/optimalRoute")
    @Produces({"application/json"})
    TransitPath findOptimalRoute(
            @QueryParam("origin") String originUnLocode,
            @QueryParam("destination") String destinationUnLocode,
            @QueryParam("deadline") String deadline);
}
