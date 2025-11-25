package com.practicalddd.cargotracker.bookingms.application.internal.commandservices;

import com.practicalddd.cargotracker.bookingms.application.internal.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.RouteCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.exceptions.CargoNotFoundException;
import com.practicalddd.cargotracker.bookingms.domain.model.exceptions.RouteNotFoundException;
import com.practicalddd.cargotracker.bookingms.domain.model.factory.CargoFactory;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.model.services.ExternalRoutingService;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class CargoBookingCommandService {

    @Inject
    private CargoRepository cargoRepository;
    
    @Inject
    private DomainEventPublisher eventPublisher;
    
    @Inject
    private ExternalRoutingService externalRoutingService;

    @Transactional
    public BookingId bookCargo(BookCargoCommand bookCargoCommand) {
        String bookingId = cargoRepository.nextBookingId();
        
        Cargo cargo = CargoFactory.createCargo(bookCargoCommand, bookingId);
        cargoRepository.store(cargo);

        eventPublisher.publish(new CargoBookedEvent(bookingId));
        return new BookingId(bookingId);
    }

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