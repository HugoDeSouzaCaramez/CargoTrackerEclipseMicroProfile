package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoQueryPort;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.CargoSummaryResource;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para consultas de cargas.
 * Apenas operações de leitura (HTTP GET).
 */
@Path("/cargoqueries")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class CargoQueryController {

    private final CargoQueryPort cargoQueryPort;

    @Inject
    public CargoQueryController(CargoQueryPort cargoQueryPort) {
        this.cargoQueryPort = cargoQueryPort;
    }

    @GET
    @Path("/cargos")
    public Response getAllCargos() {
        List<Cargo> cargos = cargoQueryPort.findAllCargos();
        
        List<CargoSummaryResource> response = cargos.stream()
                .map(this::toSummaryResource)
                .collect(Collectors.toList());
        
        return Response.ok(response).build();
    }

    @GET
    @Path("/bookingIds")
    public Response getAllBookingIds() {
        List<BookingId> bookingIds = cargoQueryPort.getAllBookingIds();
        
        List<String> response = bookingIds.stream()
                .map(BookingId::getBookingId)
                .collect(Collectors.toList());
        
        return Response.ok(response).build();
    }

    @GET
    @Path("/cargos/{bookingId}")
    public Response getCargoByBookingId(@PathParam("bookingId") String bookingId) {
        Cargo cargo = cargoQueryPort.findCargoByBookingId(bookingId);
        return Response.ok(toDetailResource(cargo)).build();
    }

    private CargoSummaryResource toSummaryResource(Cargo cargo) {
        return new CargoSummaryResource(
            cargo.getBookingId().getBookingId(),
            cargo.getRouteSpecification().getOrigin().getUnLocCode(),
            cargo.getRouteSpecification().getDestination().getUnLocCode(),
            cargo.getDelivery().getTransportStatus().name()
        );
    }

    private Object toDetailResource(Cargo cargo) {
        // Em produção, criar DTO específico
        return cargo;
    }
}
