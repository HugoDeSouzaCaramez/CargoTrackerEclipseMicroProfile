package com.practicalddd.cargotracker.bookingms.application.commandservices;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoStatusChangedEvent;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.factory.CargoFactory;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para operações em lote que necessitam de controle transacional
 * explícito.
 */
@ApplicationScoped
public class CargoBulkOperationService {

    @Inject
    private CargoRepository cargoRepository;

    @Inject
    private com.practicalddd.cargotracker.bookingms.application.events.DomainEventPublisher eventPublisher;

    @Inject
    private TransactionalService transactionalService;

    /**
     * Processa múltiplos bookings em uma única transação.
     * Se algum falhar, todos são revertidos.
     */
    public List<BookingId> processBulkBookings(List<BookCargoCommand> commands) {
        List<BookingId> results = new ArrayList<>();

        try {
            transactionalService.executeInTransaction(() -> {
                for (BookCargoCommand command : commands) {
                    // Validações específicas para bulk
                    validateBulkBookingCommand(command);

                    // Processa cada booking individualmente
                    BookingId bookingId = processSingleBookingInTransaction(command);
                    results.add(bookingId);
                }
            });

            return results;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Bulk booking operation failed. No bookings were processed.", e);
        }
    }

    /**
     * Atualiza múltiplos cargos em uma transação.
     */
    public void bulkUpdateCargos(Runnable... updateOperations) {
        try {
            transactionalService.executeInTransaction(updateOperations);
        } catch (Exception e) {
            throw new RuntimeException("Bulk update failed", e);
        }
    }

    /**
     * Processa um único booking dentro da transação existente
     */
    private BookingId processSingleBookingInTransaction(BookCargoCommand command) {
        // Validações comuns
        if (command.getDestArrivalDeadline().isBefore(LocalDateTime.now().plusDays(1))) {
            throw new IllegalArgumentException("Arrival deadline must be at least 24 hours from now");
        }

        // Gera ID único
        String bookingIdStr = cargoRepository.nextBookingId();

        // Cria e armazena o cargo
        Cargo cargo = CargoFactory.createCargo(command, bookingIdStr);
        cargoRepository.store(cargo);

        // Publica evento de booking
        eventPublisher.publish(new CargoBookedEvent(
            bookingIdStr,
            command.getBookingAmount(),
            command.getOriginLocation(),
            command.getDestLocation(),
            command.getDestArrivalDeadline()
        ));
        
        // Publica evento de status
        eventPublisher.publish(new CargoStatusChangedEvent(
            bookingIdStr,
            null,
            "BOOKED",
            "Cargo booked in bulk operation"
        ));

        return new BookingId(bookingIdStr);
    }

    private void validateBulkBookingCommand(BookCargoCommand command) {
        if (command.getBookingAmount() > 1000) {
            throw new IllegalArgumentException("Bulk bookings cannot exceed 1000 units per booking");
        }

        if (command.getBookingAmount() < 10) {
            throw new IllegalArgumentException("Bulk bookings must have at least 10 units");
        }
    }
}
