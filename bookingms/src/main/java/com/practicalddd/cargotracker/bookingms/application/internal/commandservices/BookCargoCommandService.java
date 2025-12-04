package com.practicalddd.cargotracker.bookingms.application.internal.commandservices;

import com.practicalddd.cargotracker.bookingms.application.internal.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoBookingCommandPort;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.factory.CargoFactory;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Serviço de aplicação para processar comandos de booking de cargas.
 * Implementa apenas operações de escrita (commands).
 */
@ApplicationScoped
public class BookCargoCommandService implements CargoBookingCommandPort {

    @Inject
    private CargoRepository cargoRepository;
    
    @Inject
    private DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingId bookCargo(BookCargoCommand bookCargoCommand) {
        String bookingId = cargoRepository.nextBookingId();
        
        Cargo cargo = CargoFactory.createCargo(bookCargoCommand, bookingId);
        cargoRepository.store(cargo);

        eventPublisher.publish(new CargoBookedEvent(bookingId));
        return new BookingId(bookingId);
    }
}
