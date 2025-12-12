package com.practicalddd.cargotracker.bookingms.interfaces.rest.transform;

import com.practicalddd.cargotracker.bookingms.domain.model.commands.RouteCargoCommand;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.RouteCargoResource;

public class RouteCargoCommandDTOAssembler {

    public static RouteCargoCommand toCommandFromDTO(RouteCargoResource routeCargoResource) {
        // Normalizar o ID para garantir formato consistente
        String normalizedBookingId = routeCargoResource.getBookingId() != null 
            ? routeCargoResource.getBookingId().trim().toUpperCase() 
            : null;
            
        return new RouteCargoCommand(normalizedBookingId);
    }
}
