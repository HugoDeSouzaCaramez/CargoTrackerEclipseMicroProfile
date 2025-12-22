package com.practicalddd.cargotracker.bookingms.application.services;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoBookingCommandPort;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.DeadlinePolicy;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.Location;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.factory.CargoFactory;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoStatusChangedEvent;
import com.practicalddd.cargotracker.bookingms.application.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.domain.services.CargoPortValidationService;
import com.practicalddd.cargotracker.bookingms.domain.services.RouteFeasibilityService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class CargoBookingCommandServiceImpl implements CargoBookingCommandPort {

    @Inject
    private CargoRepository cargoRepository;
    
    @Inject
    private DomainEventPublisher eventPublisher;
    
    @Inject
    private CargoPortValidationService cargoPortValidationService;
    
    @Inject
    private RouteFeasibilityService routeFeasibilityService;

    @Override
    @Transactional
    public BookingId bookCargo(BookCargoCommand bookCargoCommand) {
        // 1. Validação cross-aggregate (portos)
        CargoPortValidationService.ValidationResult validationResult = 
            cargoPortValidationService.validateBookingFeasibility(
                bookCargoCommand.getOriginLocation(),
                bookCargoCommand.getDestLocation(),
                bookCargoCommand.getBookingAmount()
            );
        
        validationResult.throwIfInvalid();
        
        if (validationResult.hasWarnings()) {
            System.out.println("[WARNING] Booking validation warnings: " + 
                             validationResult.getWarningSummary());
        }

        // 2. Validação de viabilidade da rota (prazo mínimo baseado no tipo de rota)
        Location origin = new Location(bookCargoCommand.getOriginLocation());
        Location destination = new Location(bookCargoCommand.getDestLocation());
        
        RouteFeasibilityService.ValidationResult routeValidation = 
            routeFeasibilityService.validateRouteFeasibility(
                origin,
                destination,
                bookCargoCommand.getDestArrivalDeadline()
            );
        
        routeValidation.throwIfInvalid();
        
        // Log warnings e informações
        if (routeValidation.hasWarnings()) {
            System.out.println("[ROUTE WARNING] " + routeValidation.getWarning());
        }
        
        if (routeValidation.getInfo() != null) {
            System.out.println("[ROUTE INFO] " + routeValidation.getInfo());
        }

        // Gera ID único
        String bookingIdStr = cargoRepository.nextBookingId();
        
        // Log de prioridade para monitoramento
        System.out.println(String.format(
            "[INFO] Booking %s criado com prioridade %s. Rota: %s -> %s (%s)",
            bookingIdStr, 
            routeValidation.getPriorityCategory(),
            origin.getUnLocCode(),
            destination.getUnLocCode(),
            routeValidation.isIntercontinental() ? "intercontinental" : "regional"
        ));

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
            String.format("Cargo booked with %s priority", routeValidation.getPriorityCategory())
        ));
        
        return new BookingId(bookingIdStr);
    }
}
