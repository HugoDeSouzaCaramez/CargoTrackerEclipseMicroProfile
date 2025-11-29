package com.practicalddd.cargotracker.handlingms.interfaces.rest;

import com.practicalddd.cargotracker.handlingms.application.internal.commandservices.HandlingActivityRegistrationCommandService;
import com.practicalddd.cargotracker.handlingms.interfaces.rest.dto.HandlingActivityRegistrationResource;
import com.practicalddd.cargotracker.handlingms.interfaces.rest.transform.HandlingActivityRegistrationCommandDTOAssembler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/cargohandling")
@ApplicationScoped
public class CargoHandlingController {

    @Inject
    private HandlingActivityRegistrationCommandService handlingActivityRegistrationCommandService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerHandlingActivity(HandlingActivityRegistrationResource handlingActivityRegistrationResource) {
        handlingActivityRegistrationCommandService.registerHandlingActivityService(
            HandlingActivityRegistrationCommandDTOAssembler.toCommandFromDTO(handlingActivityRegistrationResource));
        
        return Response.ok()
                .entity("Handling Activity Registered")
                .build();
    }
}
