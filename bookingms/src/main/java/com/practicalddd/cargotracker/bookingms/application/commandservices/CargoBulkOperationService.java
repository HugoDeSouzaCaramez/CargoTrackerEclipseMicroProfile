package com.practicalddd.cargotracker.bookingms.application.commandservices;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoStatusChangedEvent;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.factory.CargoFactory;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.DeadlinePolicy;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.Location;
import com.practicalddd.cargotracker.bookingms.domain.services.CargoPortValidationService;
import com.practicalddd.cargotracker.bookingms.domain.services.RouteFeasibilityService;

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
    
    @Inject
    private CargoPortValidationService cargoPortValidationService;
    
    @Inject
    private RouteFeasibilityService routeFeasibilityService;

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
                    
                    // Validação cross-aggregate para cada comando
                    CargoPortValidationService.ValidationResult validationResult = 
                        cargoPortValidationService.validateBookingFeasibility(
                            command.getOriginLocation(),
                            command.getDestLocation(),
                            command.getBookingAmount()
                        );
                    
                    validationResult.throwIfInvalid();
                    
                    if (validationResult.hasWarnings()) {
                        System.out.println("[WARNING] Bulk booking validation: " + 
                                         validationResult.getWarningSummary());
                    }

                    // Validação de viabilidade da rota
                    Location origin = new Location(command.getOriginLocation());
                    Location destination = new Location(command.getDestLocation());
                    
                    RouteFeasibilityService.ValidationResult routeValidation = 
                        routeFeasibilityService.validateRouteFeasibility(
                            origin,
                            destination,
                            command.getDestArrivalDeadline()
                        );
                    
                    routeValidation.throwIfInvalid();
                    
                    // Restrições adicionais para bulk
                    if (routeValidation.isIntercontinental() && 
                        routeValidation.getPriorityCategory().equals("URGENT")) {
                        throw new IllegalArgumentException(
                            "Bulk bookings não permitem cargas urgentes em rotas intercontinentais"
                        );
                    }

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
     * Processa um único booking dentro da transação existente
     */
    private BookingId processSingleBookingInTransaction(BookCargoCommand command) {
        // Validações já foram feitas no método principal
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
        
        // Validação adicional de prazo para bulk
        boolean isIntercontinental = !command.getOriginLocation().substring(0, 2)
            .equals(command.getDestLocation().substring(0, 2));
        
        DeadlinePolicy policy = command.getDeadlinePolicy(isIntercontinental);
        
        if (policy.getRemainingDays() < 7) {
            throw new IllegalArgumentException(
                "Bulk bookings require at least 7 days for planning. Remaining: " + 
                policy.getRemainingDays() + " days"
            );
        }
    }
}
