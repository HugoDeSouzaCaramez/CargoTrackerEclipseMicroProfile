package com.practicalddd.cargotracker.bookingms.application.internal.commandservices;

import com.practicalddd.cargotracker.bookingms.application.internal.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.application.internal.validators.CargoValidator;
import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoRoutingCommandPort;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.ExternalRoutingService;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.RouteCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoStatusChangedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.exceptions.CargoNotFoundException;
import com.practicalddd.cargotracker.bookingms.domain.model.exceptions.RouteNotFoundException;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.EventStoreRepository;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Serviço de aplicação para processar comandos de roteamento de cargas.
 * Implementa validações cross-aggregate e controle transacional explícito.
 */
@ApplicationScoped
public class RouteCargoCommandService implements CargoRoutingCommandPort {

    private static final Logger logger = Logger.getLogger(RouteCargoCommandService.class.getName());
    
    @Inject
    private CargoRepository cargoRepository;

    @Inject
    private DomainEventPublisher eventPublisher;

    @Inject
    private ExternalRoutingService externalRoutingService;

    @Inject
    private TransactionalService transactionalService;

    @Inject
    private CargoValidator cargoValidator;

    @Inject
    private EventStoreRepository eventStoreRepository;

    @Override
    @Transactional
    public void assignRouteToCargo(RouteCargoCommand routeCargoCommand) {
        // Validações de aplicação usando validador centralizado
        validateRoutingCommand(routeCargoCommand);

        Cargo cargo = cargoRepository.find(new BookingId(routeCargoCommand.getCargoBookingId()))
                .orElseThrow(() -> new CargoNotFoundException(routeCargoCommand.getCargoBookingId()));

        // Validações cross-aggregate
        validateCargoStateForRouting(cargo);
        validateReRoutingFrequency(cargo);
        validateCargoCapacityConstraints(cargo);

        // Buscar rota do serviço externo
        CargoItinerary cargoItinerary = externalRoutingService
                .fetchRouteForSpecification(cargo.getRouteSpecification());

        if (cargoItinerary == null || cargoItinerary.getLegs().isEmpty()) {
            throw new RouteNotFoundException(
                    cargo.getRouteSpecification().getOrigin().getUnLocCode(),
                    cargo.getRouteSpecification().getDestination().getUnLocCode());
        }

        // Validações do itinerário
        validateItinerary(cargoItinerary, cargo);

        // Atualizar estado do cargo
        String oldStatus = cargo.getStatus().name();
        cargo.assignToRoute(cargoItinerary);
        cargoRepository.store(cargo);
        String newStatus = cargo.getStatus().name();

        // Publicar eventos de domínio enriquecidos
        publishRoutingEvents(routeCargoCommand.getCargoBookingId(), cargoItinerary, oldStatus, newStatus, cargo);

        logger.info(String.format("Route assigned to cargo %s with %d legs",
                routeCargoCommand.getCargoBookingId(), cargoItinerary.getLegs().size()));
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

                validateCargoStateForRouting(cargo);
                validateReRoutingFrequency(cargo);
                validateCargoCapacityConstraints(cargo);

                CargoItinerary cargoItinerary = externalRoutingService
                        .fetchRouteForSpecification(cargo.getRouteSpecification());

                if (cargoItinerary.getLegs().isEmpty()) {
                    throw new RouteNotFoundException(
                            cargo.getRouteSpecification().getOrigin().getUnLocCode(),
                            cargo.getRouteSpecification().getDestination().getUnLocCode());
                }

                validateItinerary(cargoItinerary, cargo);

                String oldStatus = cargo.getStatus().name();
                cargo.assignToRoute(cargoItinerary);
                cargoRepository.store(cargo);
                String newStatus = cargo.getStatus().name();

                publishRoutingEvents(routeCargoCommand.getCargoBookingId(), cargoItinerary, oldStatus, newStatus, cargo);

                logger.info(String.format("Route assigned (explicit transaction) to cargo %s with %d legs",
                        routeCargoCommand.getCargoBookingId(), cargoItinerary.getLegs().size()));
            });
        } catch (CargoNotFoundException | RouteNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            // Relançar validações de negócio
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign route with explicit transaction", e);
        }
    }

    /**
     * Validações específicas da camada de aplicação
     */
    private void validateRoutingCommand(RouteCargoCommand routeCargoCommand) {
        if (routeCargoCommand.getCargoBookingId() == null ||
                routeCargoCommand.getCargoBookingId().trim().isEmpty()) {
            throw new IllegalArgumentException("Cargo booking ID cannot be null or empty");
        }

        if (routeCargoCommand.getCargoBookingId().length() < 5) {
            throw new IllegalArgumentException("Cargo booking ID must be at least 5 characters");
        }

        // Validar formato do booking ID (deve começar com 3 letras seguidas de números)
        if (!routeCargoCommand.getCargoBookingId().matches("^[A-Z]{3}[0-9]+$")) {
            throw new IllegalArgumentException(
                    "Invalid booking ID format. Expected: 3 uppercase letters followed by numbers");
        }

        // Validação de prazo de roteamento
        if (routeCargoCommand.getRoutingDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Routing deadline has already passed");
        }

        // Prazo não pode ser muito longo (ex: mais de 30 dias)
        if (ChronoUnit.DAYS.between(LocalDateTime.now(), routeCargoCommand.getRoutingDeadline()) > 30) {
            throw new IllegalArgumentException("Routing deadline cannot be more than 30 days in the future");
        }

        // Prazo mínimo (ex: pelo menos 1 hora para processamento)
        if (ChronoUnit.HOURS.between(LocalDateTime.now(), routeCargoCommand.getRoutingDeadline()) < 1) {
            throw new IllegalArgumentException("Routing deadline must be at least 1 hour from now");
        }
    }

    /**
     * Validações cross-aggregate: frequência de re-roteamento
     */
    private void validateReRoutingFrequency(Cargo cargo) {
        if (cargo.getItinerary() != null && !cargo.getItinerary().isEmpty()) {
            // Buscar eventos de roteamento anteriores
            Optional<LocalDateTime> lastRoutingEvent = findLastRoutingEvent(cargo.getBookingId().getBookingId());
            
            if (lastRoutingEvent.isPresent()) {
                long hoursSinceLastRouting = ChronoUnit.HOURS.between(lastRoutingEvent.get(), LocalDateTime.now());
                
                if (hoursSinceLastRouting < 24) {
                    throw new IllegalArgumentException(
                            String.format("Cargo was routed %d hours ago. Re-routing is only allowed after 24 hours.", 
                                         hoursSinceLastRouting));
                }
            } else {
                // Se tem itinerário mas não encontrou evento, ainda assim restringir
                logger.warning(String.format(
                        "Cargo %s has itinerary but no routing events found. Allowing re-routing with caution.",
                        cargo.getBookingId().getBookingId()));
            }
            
            logger.info(String.format("Cargo %s is being re-routed (previous routing found)",
                    cargo.getBookingId().getBookingId()));
        }
    }

    /**
     * Validações cross-aggregate: estado do cargo para roteamento
     */
    private void validateCargoStateForRouting(Cargo cargo) {
        if (cargo.getStatus() == com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoStatus.CLAIMED ||
            cargo.getStatus() == com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoStatus.COMPLETED ||
            cargo.getStatus() == com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    String.format("Cannot route cargo in %s state", cargo.getStatus()));
        }

        // Verificar se o cargo está no prazo
        if (!cargo.isOnTrack()) {
            throw new IllegalArgumentException(
                    "Cargo is behind schedule. Please update the arrival deadline before routing.");
        }
    }

    /**
     * Validações cross-aggregate: capacidade e restrições
     */
    private void validateCargoCapacityConstraints(Cargo cargo) {
        // Verificar capacidade do porto de origem
        cargoValidator.validatePortRestrictions(
                cargo.getRouteSpecification().getOrigin().getUnLocCode(),
                cargo.getBookingAmount().getBookingAmount(),
                cargo.getRouteSpecification().getCargoType().name()
        );

        // Verificar capacidade do porto de destino
        cargoValidator.validatePortRestrictions(
                cargo.getRouteSpecification().getDestination().getUnLocCode(),
                cargo.getBookingAmount().getBookingAmount(),
                cargo.getRouteSpecification().getCargoType().name()
        );

        // Verificar disponibilidade de capacidade no período estimado
        LocalDateTime estimatedDeparture = LocalDateTime.now().plusHours(2); // Estimativa
        LocalDateTime estimatedArrival = cargo.getRouteSpecification().getArrivalDeadline();
        
        cargoValidator.validateCapacityAvailability(
                cargo.getRouteSpecification().getOrigin().getUnLocCode(),
                estimatedDeparture,
                estimatedArrival,
                cargo.getBookingAmount().getBookingAmount()
        );
    }

    /**
     * Validações do itinerário
     */
    private void validateItinerary(CargoItinerary itinerary, Cargo cargo) {
        if (itinerary == null) {
            throw new IllegalArgumentException("Itinerary cannot be null");
        }

        if (itinerary.getLegs().isEmpty()) {
            throw new IllegalArgumentException("Itinerary must have at least one leg");
        }

        // Verificar compatibilidade com o tipo de carga
        validateCargoTypeCompatibility(itinerary, cargo);

        // Verificar restrições temporais
        validateItineraryTiming(itinerary, cargo);

        // Verificar disponibilidade de recursos (vessels, equipamentos)
        validateResourceAvailability(itinerary);
    }

    /**
     * Valida compatibilidade do tipo de carga com o itinerário
     */
    private void validateCargoTypeCompatibility(CargoItinerary itinerary, Cargo cargo) {
        // Implementar lógica baseada no tipo de carga
        // Ex: cargas perigosas têm restrições específicas
        com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoType cargoType = 
                cargo.getRouteSpecification().getCargoType();
        
        if (cargoType == com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoType.HAZARDOUS) {
            // Verificar se todos os legs permitem cargas perigosas
            itinerary.getLegs().forEach(leg -> {
                // Simulação: verificar se o vessel permite cargas perigosas
                if (leg.getVoyage() != null && leg.getVoyage().getVoyageNumber().contains("NO_HAZ")) {
                    throw new IllegalArgumentException(
                            String.format("Voyage %s does not accept hazardous cargo",
                                        leg.getVoyage().getVoyageNumber()));
                }
            });
        }
        
        if (cargoType == com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoType.REFRIGERATED) {
            // Verificar equipamento de refrigeração
            itinerary.getLegs().forEach(leg -> {
                if (leg.getTransportMode() == com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.TransportMode.AIR) {
                    throw new IllegalArgumentException(
                            "Refrigerated cargo cannot be transported by air in this itinerary");
                }
            });
        }
    }

    /**
     * Valida timing do itinerário
     */
    private void validateItineraryTiming(CargoItinerary itinerary, Cargo cargo) {
        LocalDateTime arrivalDeadline = cargo.getRouteSpecification().getArrivalDeadline();
        
        // Calcular tempo total do itinerário
        long totalHours = itinerary.getLegs().stream()
                .mapToLong(leg -> ChronoUnit.HOURS.between(leg.getLoadTime(), leg.getUnloadTime()))
                .sum();
        
        // Adicionar buffer para operações portuárias (12 horas por escala)
        long bufferHours = (itinerary.getLegs().size() - 1) * 12L;
        
        LocalDateTime estimatedArrival = LocalDateTime.now()
                .plusHours(totalHours)
                .plusHours(bufferHours);
        
        if (estimatedArrival.isAfter(arrivalDeadline)) {
            throw new IllegalArgumentException(
                    String.format("Itinerary exceeds deadline. Estimated arrival: %s, Deadline: %s",
                                 estimatedArrival, arrivalDeadline));
        }
    }

    /**
     * Valida disponibilidade de recursos
     */
    private void validateResourceAvailability(CargoItinerary itinerary) {
        // TODO: Integrar com serviço de disponibilidade de recursos
        // Por enquanto, apenas log
        itinerary.getLegs().forEach(leg -> {
            logger.fine(String.format(
                    "Validating resource availability for voyage %s from %s to %s",
                    leg.getVoyage().getVoyageNumber(),
                    leg.getLoadLocation().getUnLocCode(),
                    leg.getUnloadLocation().getUnLocCode()
            ));
        });
    }

    /**
     * Busca o último evento de roteamento do cargo
     */
    private Optional<LocalDateTime> findLastRoutingEvent(String bookingId) {
        // Buscar eventos CargoRoutedEvent do event store
        return eventStoreRepository.findByAggregateId(bookingId).stream()
                .filter(event -> event.eventType().equals("CargoRoutedEvent"))
                .map(event -> event.occurredOn())
                .max(LocalDateTime::compareTo);
    }

    /**
     * Publica eventos relacionados ao roteamento
     */
    private void publishRoutingEvents(String bookingId, CargoItinerary itinerary, 
                                     String oldStatus, String newStatus, Cargo cargo) {
        // Publicar evento de domínio enriquecido
        int legCount = itinerary != null ? itinerary.getLegs().size() : 0;
        eventPublisher.publish(new CargoRoutedEvent(
                bookingId,
                legCount,
                LocalDateTime.now(),
                calculateItinerarySummary(itinerary)
        ));
        
        // Publicar evento de mudança de status com informações detalhadas
        eventPublisher.publish(new CargoStatusChangedEvent(
                bookingId,
                oldStatus,
                newStatus,
                String.format("Route assigned with %d legs. Estimated transit: %d hours",
                             legCount, cargo.calculateEstimatedTransitTime())
        ));
        
        // Publicar evento específico para cross-aggregate (se necessário)
        publishCrossAggregateEvents(cargo, itinerary);
    }

    /**
     * Calcula resumo do itinerário para enriquecer eventos
     */
    private String calculateItinerarySummary(CargoItinerary itinerary) {
        if (itinerary == null || itinerary.getLegs().isEmpty()) {
            return "Empty itinerary";
        }
        
        long totalHours = itinerary.getLegs().stream()
                .mapToLong(leg -> ChronoUnit.HOURS.between(leg.getLoadTime(), leg.getUnloadTime()))
                .sum();
        
        String firstLeg = itinerary.getLegs().get(0).getLoadLocation().getUnLocCode();
        String lastLeg = itinerary.getLegs().get(itinerary.getLegs().size() - 1)
                .getUnloadLocation().getUnLocCode();
        
        return String.format("%s -> %s via %d legs (%d hours total)",
                            firstLeg, lastLeg, itinerary.getLegs().size(), totalHours);
    }

    /**
     * Publica eventos para outros bounded contexts
     */
    private void publishCrossAggregateEvents(Cargo cargo, CargoItinerary itinerary) {
        // TODO: Implementar eventos para:
        // 1. Serviço de tracking
        // 2. Serviço de billing (atualizar tarifas)
        // 3. Serviço de notificação
        // 4. Serviço de compliance
        
        logger.fine(String.format(
                "[CROSS-AGGREGATE] Cargo %s routed: %d legs, amount: %d, type: %s",
                cargo.getBookingId().getBookingId(),
                itinerary.getLegs().size(),
                cargo.getBookingAmount().getBookingAmount(),
                cargo.getRouteSpecification().getCargoType()
        ));
    }
}
