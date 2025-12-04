package com.practicalddd.cargotracker.bookingms.application.internal.commandservices;

import com.practicalddd.cargotracker.bookingms.application.internal.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoRoutingCommandPort;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.ExternalRoutingService;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.RouteCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.exceptions.CargoNotFoundException;
import com.practicalddd.cargotracker.bookingms.domain.model.exceptions.RouteNotFoundException;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Serviço de aplicação para processar comandos de roteamento de cargas.
 * Implementa apenas operações de escrita (commands).
 */
@ApplicationScoped
public class RouteCargoCommandService implements CargoRoutingCommandPort {

    @Inject
    private CargoRepository cargoRepository;
    
    @Inject
    private DomainEventPublisher eventPublisher;
    
    @Inject
    private ExternalRoutingService externalRoutingService;

    @Override
    @Transactional
    public void assignRouteToCargo(RouteCargoCommand routeCargoCommand) {
        Cargo cargo = cargoRepository.find(new BookingId(routeCargoCommand.getCargoBookingId()))
                .orElseThrow(() -> new CargoNotFoundException(routeCargoCommand.getCargoBookingId()));

        CargoItinerary cargoItinerary = externalRoutingService
                .fetchRouteForSpecification(cargo.getRouteSpecification());

        if (cargoItinerary.getLegs().isEmpty()) {
            throw new RouteNotFoundException(
                cargo.getRouteSpecification().getOrigin().getUnLocCode(),
                cargo.getRouteSpecification().getDestination().getUnLocCode()
            );
        }

        cargo.assignToRoute(cargoItinerary);
        cargoRepository.store(cargo);

        eventPublisher.publish(new CargoRoutedEvent(routeCargoCommand.getCargoBookingId()));
    }
}
