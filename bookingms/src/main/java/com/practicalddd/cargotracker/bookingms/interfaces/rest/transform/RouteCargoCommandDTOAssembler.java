package com.practicalddd.cargotracker.bookingms.interfaces.rest.transform;

import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.RouteCargoCommand;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.BookCargoResource;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.RouteCargoResource;


public class RouteCargoCommandDTOAssembler {

    public static RouteCargoCommand toCommandFromDTO(RouteCargoResource routeCargoResource){

        return new RouteCargoCommand(routeCargoResource.getBookingId());
    }
}
