package com.practicalddd.cargotracker.bookingms.application.services;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoBookingCommandPort;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.factory.CargoFactory;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoStatusChangedEvent;
import com.practicalddd.cargotracker.bookingms.application.events.DomainEventPublisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class CargoBookingCommandServiceImpl implements CargoBookingCommandPort {

    @Inject
    private CargoRepository cargoRepository;
    
    @Inject
    private DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingId bookCargo(BookCargoCommand bookCargoCommand) {
        // Gera ID único
        String bookingIdStr = cargoRepository.nextBookingId();
        
        // Cria e armazena o cargo
        Cargo cargo = CargoFactory.createCargo(bookCargoCommand, bookingIdStr);
        cargoRepository.store(cargo);
        
        // Publica evento de booking
        eventPublisher.publish(new CargoBookedEvent(
            bookingIdStr,
            bookCargoCommand.getBookingAmount(),
            bookCargoCommand.getOriginLocation(),
            bookCargoCommand.getDestLocation(),
            bookCargoCommand.getDestArrivalDeadline()
        ));
        
        // Publica evento de status
        eventPublisher.publish(new CargoStatusChangedEvent(
            bookingIdStr,
            null,
            "BOOKED",
            "Cargo booked"
        ));
        
        return new BookingId(bookingIdStr);
    }
}
