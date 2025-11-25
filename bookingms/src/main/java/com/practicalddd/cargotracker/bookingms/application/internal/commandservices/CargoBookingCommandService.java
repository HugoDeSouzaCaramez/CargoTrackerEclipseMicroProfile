package com.practicalddd.cargotracker.bookingms.application.internal.commandservices;

import com.practicalddd.cargotracker.bookingms.application.internal.outboundservices.acl.ExternalCargoRoutingService;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.RouteCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary;
import com.practicalddd.cargotracker.shareddomain.events.CargoBookedEvent;
import com.practicalddd.cargotracker.shareddomain.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.shareddomain.events.CargoRoutedEventData;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class CargoBookingCommandService {

    @Inject
    private CargoRepository cargoRepository;
    
    @Inject
    private Event<CargoBookedEvent> cargoBookedEventControl;
    
    @Inject
    private Event<CargoRoutedEvent> cargoRoutedEventControl;
    
    @Inject
    private ExternalCargoRoutingService externalCargoRoutingService;

    @Transactional
    public BookingId bookCargo(BookCargoCommand bookCargoCommand) {
        String bookingId = cargoRepository.nextBookingId();
        
        BookCargoCommand commandWithId = new BookCargoCommand(
            bookingId,
            bookCargoCommand.getBookingAmount(),
            bookCargoCommand.getOriginLocation(),
            bookCargoCommand.getDestLocation(),
            bookCargoCommand.getDestArrivalDeadline()
        );
        
        Cargo cargo = new Cargo(commandWithId);
        cargoRepository.store(cargo);

        CargoBookedEvent cargoBookedEvent = new CargoBookedEvent();
        cargoBookedEvent.setId(bookingId);
        cargoBookedEventControl.fire(cargoBookedEvent);

        return new BookingId(bookingId);
    }

    @Transactional
    public void assignRouteToCargo(RouteCargoCommand routeCargoCommand) {
        Cargo cargo = cargoRepository.find(new BookingId(routeCargoCommand.getCargoBookingId()));

        CargoItinerary cargoItinerary = externalCargoRoutingService
                .fetchRouteForSpecification(cargo.getRouteSpecification());

        if (cargoItinerary.getLegs().isEmpty()) {
            throw new RuntimeException("Nenhuma rota encontrada para a especificação fornecida. " +
                    "Origem: " + cargo.getRouteSpecification().getOrigin().getUnLocCode() + ", " +
                    "Destino: " + cargo.getRouteSpecification().getDestination().getUnLocCode() + ", " +
                    "Deadline: " + cargo.getRouteSpecification().getArrivalDeadline());
        }

        cargo.assignToRoute(cargoItinerary);
        cargoRepository.store(cargo);

        CargoRoutedEvent cargoRoutedEvent = new CargoRoutedEvent();
        CargoRoutedEventData eventData = new CargoRoutedEventData();
        eventData.setBookingId(routeCargoCommand.getCargoBookingId());
        cargoRoutedEvent.setContent(eventData);
        cargoRoutedEventControl.fire(cargoRoutedEvent);
    }
}