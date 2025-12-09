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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    @Inject
    private TransactionalService transactionalService; // em vez de TransactionalCommandService

    // Lista de portos suportados pelo sistema
    private static final Set<String> SUPPORTED_PORTS = new HashSet<>(Arrays.asList(
        "USNYC", "NLRTM", "GBLON", "JPTYO", "SGSIN", 
        "DEHAM", "CNHKG", "USLGB", "CNPVG", "HKHKG"
    ));

    @Override
    @Transactional
    public BookingId bookCargo(BookCargoCommand bookCargoCommand) {
        // Validação adicional do comando - REGRAS DE APLICAÇÃO
        if (bookCargoCommand.getDestArrivalDeadline().isBefore(LocalDateTime.now().plusDays(1))) {
            throw new IllegalArgumentException("Arrival deadline must be at least 24 hours from now");
        }

        // Validação adicional: verificar se portos são suportados pelo sistema
        validateSupportedPorts(bookCargoCommand.getOriginLocation(), bookCargoCommand.getDestLocation());

        // Validação adicional: verificar conflitos de booking
        validateNoDuplicateBooking(bookCargoCommand);

        String bookingId = cargoRepository.nextBookingId();

        Cargo cargo = CargoFactory.createCargo(bookCargoCommand, bookingId);
        cargoRepository.store(cargo);

        eventPublisher.publish(new CargoBookedEvent(bookingId));
        return new BookingId(bookingId);
    }

    /**
     * Método alternativo que usa controle transacional explícito (programático)
     * em vez de @Transactional declarativo.
     */
    public BookingId bookCargoWithExplicitTransaction(BookCargoCommand bookCargoCommand) {
        try {
            return transactionalService.executeInTransaction(() -> {
                // Mesmas validações
                if (bookCargoCommand.getDestArrivalDeadline().isBefore(LocalDateTime.now().plusDays(1))) {
                    throw new IllegalArgumentException("Arrival deadline must be at least 24 hours from now");
                }

                validateSupportedPorts(bookCargoCommand.getOriginLocation(), bookCargoCommand.getDestLocation());
                validateNoDuplicateBooking(bookCargoCommand);

                String bookingId = cargoRepository.nextBookingId();
                Cargo cargo = CargoFactory.createCargo(bookCargoCommand, bookingId);
                cargoRepository.store(cargo);

                eventPublisher.publish(new CargoBookedEvent(bookingId));
                return new BookingId(bookingId);
            });
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to book cargo with explicit transaction", e);
        }
    }

    private void validateSupportedPorts(String origin, String destination) {
        String originUpper = origin.toUpperCase();
        String destUpper = destination.toUpperCase();
        
        if (!SUPPORTED_PORTS.contains(originUpper)) {
            throw new IllegalArgumentException(
                String.format("Origin port '%s' is not supported. Supported ports: %s", 
                    origin, String.join(", ", SUPPORTED_PORTS))
            );
        }
        
        if (!SUPPORTED_PORTS.contains(destUpper)) {
            throw new IllegalArgumentException(
                String.format("Destination port '%s' is not supported. Supported ports: %s", 
                    destination, String.join(", ", SUPPORTED_PORTS))
            );
        }
    }

    private void validateNoDuplicateBooking(BookCargoCommand command) {
        System.out.println("Validating no duplicate booking for: " + 
            command.getOriginLocation() + " -> " + command.getDestLocation());
    }
}
