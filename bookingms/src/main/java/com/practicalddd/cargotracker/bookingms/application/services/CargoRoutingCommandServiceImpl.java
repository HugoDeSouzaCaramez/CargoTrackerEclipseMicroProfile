package com.practicalddd.cargotracker.bookingms.application.services;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoRoutingCommandPort;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.ExternalRoutingService;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.commands.RouteCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.CargoItinerary;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.CargoStatusChangedEvent;
import com.practicalddd.cargotracker.bookingms.application.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.domain.services.CargoPortValidationService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class CargoRoutingCommandServiceImpl implements CargoRoutingCommandPort {

    @Inject
    private CargoRepository cargoRepository;
    
    @Inject
    private ExternalRoutingService externalRoutingService;
    
    @Inject
    private DomainEventPublisher eventPublisher;
    
    @Inject
    private CargoPortValidationService cargoPortValidationService; // NOVO

    @Override
    @Transactional
    public void assignRouteToCargo(RouteCargoCommand routeCargoCommand) {
        // Encontra o cargo
        BookingId bookingId = new BookingId(routeCargoCommand.getCargoBookingId());
        Cargo cargo = cargoRepository.find(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Cargo not found with ID: " + bookingId));
        
        // Busca rota do serviço externo
        CargoItinerary itinerary = externalRoutingService.fetchRouteForSpecification(
            cargo.getRouteSpecification()
        );
        
        if (itinerary.isEmpty()) {
            throw new IllegalArgumentException("No route found for cargo: " + bookingId);
        }
        
        // NOVO: Validação dos portos no itinerário
        CargoPortValidationService.ValidationResult routeValidation = 
            cargoPortValidationService.validateRouteForCargo(
                cargo.getRouteSpecification(),
                cargo,
                cargo.getBookingAmount().getBookingAmount()
            );
        
        // Lança exceção se a validação falhar
        routeValidation.throwIfInvalid();
        
        // Loga avisos
        if (routeValidation.hasWarnings()) {
            System.out.println("[WARNING] Route validation warnings: " + 
                             routeValidation.getWarningSummary());
        }
        
        // Atribui a rota ao cargo
        cargo.assignToRoute(itinerary);
        cargoRepository.store(cargo);
        
        // Publica evento de roteamento
        eventPublisher.publish(new CargoRoutedEvent(
            bookingId.getBookingId(),
            itinerary.getLegs().size(),
            routeCargoCommand.getRoutingDeadline(),
            "Route assigned via external service"
        ));
        
        // Publica evento de status
        eventPublisher.publish(new CargoStatusChangedEvent(
            bookingId.getBookingId(),
            "BOOKED",
            "ROUTED",
            "Route assigned"
        ));
    }
}
