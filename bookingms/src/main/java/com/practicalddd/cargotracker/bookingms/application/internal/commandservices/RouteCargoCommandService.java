package com.practicalddd.cargotracker.bookingms.application.internal.commandservices;

import com.practicalddd.cargotracker.bookingms.application.internal.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoRoutingCommandPort;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.ExternalRoutingService;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.RouteCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoStatusChangedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.exceptions.CargoNotFoundException;
import com.practicalddd.cargotracker.bookingms.domain.model.exceptions.RouteNotFoundException;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Serviço de aplicação para processar comandos de roteamento de cargas.
 */
@ApplicationScoped
public class RouteCargoCommandService implements CargoRoutingCommandPort {

    @Inject
    private CargoRepository cargoRepository;

    @Inject
    private DomainEventPublisher eventPublisher;

    @Inject
    private ExternalRoutingService externalRoutingService;

    @Inject
    private TransactionalService transactionalService;

    @Override
    @Transactional
    public void assignRouteToCargo(RouteCargoCommand routeCargoCommand) {
        // Validações de aplicação
        validateRoutingCommand(routeCargoCommand);

        Cargo cargo = cargoRepository.find(new BookingId(routeCargoCommand.getCargoBookingId()))
                .orElseThrow(() -> new CargoNotFoundException(routeCargoCommand.getCargoBookingId()));

        // Validação adicional: evitar reroteamento excessivo
        validateReRoutingNotTooFrequent(cargo);

        CargoItinerary cargoItinerary = externalRoutingService
                .fetchRouteForSpecification(cargo.getRouteSpecification());

        if (cargoItinerary.getLegs().isEmpty()) {
            throw new RouteNotFoundException(
                    cargo.getRouteSpecification().getOrigin().getUnLocCode(),
                    cargo.getRouteSpecification().getDestination().getUnLocCode());
        }

        String oldStatus = cargo.getStatus().name();
        cargo.assignToRoute(cargoItinerary);
        cargoRepository.store(cargo);
        String newStatus = cargo.getStatus().name();

        // Publica evento de domínio enriquecido
        int legCount = cargoItinerary != null ? cargoItinerary.getLegs().size() : 0;
        eventPublisher.publish(new CargoRoutedEvent(
            routeCargoCommand.getCargoBookingId(),
            legCount,
            LocalDateTime.now()
        ));
        
        // Publica evento de mudança de status
        eventPublisher.publish(new CargoStatusChangedEvent(
            routeCargoCommand.getCargoBookingId(),
            oldStatus,
            newStatus,
            "Route assigned with " + legCount + " legs"
        ));
    }

    /**
     * Método alternativo com controle transacional explícito
     */
    public void assignRouteToCargoWithExplicitTransaction(RouteCargoCommand routeCargoCommand) {
        try {
            transactionalService.executeInTransaction(() -> {
                validateRoutingCommand(routeCargoCommand);

                Cargo cargo = cargoRepository.find(new BookingId(routeCargoCommand.getCargoBookingId()))
                        .orElseThrow(() -> new CargoNotFoundException(routeCargoCommand.getCargoBookingId()));

                validateReRoutingNotTooFrequent(cargo);

                CargoItinerary cargoItinerary = externalRoutingService
                        .fetchRouteForSpecification(cargo.getRouteSpecification());

                if (cargoItinerary.getLegs().isEmpty()) {
                    throw new RouteNotFoundException(
                            cargo.getRouteSpecification().getOrigin().getUnLocCode(),
                            cargo.getRouteSpecification().getDestination().getUnLocCode());
                }

                String oldStatus = cargo.getStatus().name();
                cargo.assignToRoute(cargoItinerary);
                cargoRepository.store(cargo);
                String newStatus = cargo.getStatus().name();

                // Publica evento de domínio enriquecido
                int legCount = cargoItinerary != null ? cargoItinerary.getLegs().size() : 0;
                eventPublisher.publish(new CargoRoutedEvent(
                    routeCargoCommand.getCargoBookingId(),
                    legCount,
                    LocalDateTime.now()
                ));
                
                // Publica evento de mudança de status
                eventPublisher.publish(new CargoStatusChangedEvent(
                    routeCargoCommand.getCargoBookingId(),
                    oldStatus,
                    newStatus,
                    "Route assigned with " + legCount + " legs"
                ));
            });
        } catch (CargoNotFoundException | RouteNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign route with explicit transaction", e);
        }
    }

    /**
     * Validações específicas da camada de aplicação
     */
    private void validateRoutingCommand(RouteCargoCommand routeCargoCommand) {
        // Validação adicional: verificar se o prazo de roteamento é válido
        if (routeCargoCommand.getRoutingDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Routing deadline has already passed");
        }

        // Validação: prazo não pode ser muito longo (ex: mais de 30 dias)
        if (ChronoUnit.DAYS.between(LocalDateTime.now(), routeCargoCommand.getRoutingDeadline()) > 30) {
            throw new IllegalArgumentException("Routing deadline cannot be more than 30 days in the future");
        }

        // Validação: booking ID deve ter formato válido
        if (routeCargoCommand.getCargoBookingId() == null ||
                routeCargoCommand.getCargoBookingId().trim().isEmpty()) {
            throw new IllegalArgumentException("Cargo booking ID cannot be null or empty");
        }

        if (routeCargoCommand.getCargoBookingId().length() < 5) {
            throw new IllegalArgumentException("Cargo booking ID is too short");
        }
    }

    /**
     * Valida se o cargo não foi roteado recentemente
     */
    private void validateReRoutingNotTooFrequent(Cargo cargo) {
        if (cargo.getItinerary() != null && !cargo.getItinerary().isEmpty()) {
            System.out.println("Warning: Cargo " + cargo.getBookingId().getBookingId() +
                    " is being re-routed.");
        }
    }
}
