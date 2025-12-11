package com.practicalddd.cargotracker.bookingms.domain.model.aggregates;

import com.practicalddd.cargotracker.bookingms.application.internal.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.entities.Leg;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.*;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cargo {
    private BookingId bookingId;
    private final BookingAmount bookingAmount;
    private final RouteSpecification routeSpecification;
    private CargoItinerary itinerary;
    private Delivery delivery;
    private CargoStatus status;
    
    @Inject
    private DomainEventPublisher eventPublisher;

    public Cargo(BookCargoCommand bookCargoCommand) {
        validateBookingCommand(bookCargoCommand);
        
        this.bookingId = new BookingId(bookCargoCommand.getBookingId());
        this.routeSpecification = new RouteSpecification(
                new Location(bookCargoCommand.getOriginLocation()),
                new Location(bookCargoCommand.getDestLocation()),
                bookCargoCommand.getDestArrivalDeadline()
        );
        this.bookingAmount = new BookingAmount(bookCargoCommand.getBookingAmount());
        this.itinerary = new CargoItinerary(new ArrayList<>());
        this.delivery = Delivery.derivedFrom(this.routeSpecification, this.itinerary, LastCargoHandledEvent.EMPTY);
        this.status = CargoStatus.BOOKED;
    }

    private void validateBookingCommand(BookCargoCommand command) {
        if (command.getDestArrivalDeadline().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new IllegalArgumentException("Arrival deadline must be at least 24 hours from now");
        }
    }

    public void assignToRoute(CargoItinerary cargoItinerary) {
        if (cargoItinerary == null || cargoItinerary.getLegs().isEmpty()) {
            throw new IllegalArgumentException("Itinerary cannot be null or empty");
        }
        
        if (status != CargoStatus.BOOKED && status != CargoStatus.ROUTED) {
            throw new IllegalStateException("Cargo must be in BOOKED or ROUTED state to assign route");
        }
        
        // Validar que o itinerário atende à especificação de rota
        if (!routeSpecification.isSatisfiedBy(cargoItinerary)) {
            throw new IllegalArgumentException("Itinerary does not satisfy route specification");
        }
        
        // Validar que o itinerário é temporalmente viável
        validateItineraryTemporalFeasibility(cargoItinerary);
        
        String oldStatus = this.status.name();
        this.itinerary = cargoItinerary;
        this.delivery = this.delivery.updateOnRouting(this.routeSpecification, this.itinerary);
        this.status = CargoStatus.ROUTED;
        
        // Emitir evento de status
        if (eventPublisher != null) {
            eventPublisher.publish(new com.practicalddd.cargotracker.bookingms.domain.model.events.CargoStatusChangedEvent(
                this.bookingId.getBookingId(),
                oldStatus,
                this.status.name(),
                "Route assigned with " + cargoItinerary.getLegs().size() + " legs"
            ));
        }
    }

    public void deriveDeliveryProgress(LastCargoHandledEvent lastCargoHandledEvent) {
        validateHandlingEvent(lastCargoHandledEvent);
        this.delivery = this.delivery.updateOnHandling(lastCargoHandledEvent);
        updateStatusBasedOnDelivery();
    }

    public void changeDestination(Location newDestination, LocalDateTime newDeadline) {
        if (status == CargoStatus.CLAIMED || status == CargoStatus.COMPLETED) {
            throw new IllegalStateException("Cannot change destination for claimed or completed cargo");
        }
        
        RouteSpecification newRouteSpec = new RouteSpecification(
            this.routeSpecification.getOrigin(),
            newDestination,
            newDeadline
        );
        
        // Isso poderá disparar uma necessidade de reroteamento
        // Por enquanto, apenas atualiza a especificação
        // Isso pode ser um evento de domínio "CargoDestinationChanged"
    }

    public boolean isMisdirected() {
        return delivery.getRoutingStatus() == RoutingStatus.MISROUTED;
    }

    public boolean isReadyForClaim() {
        return delivery.getTransportStatus() == TransportStatus.IN_PORT &&
               delivery.getLastKnownLocation().equals(routeSpecification.getDestination());
    }

    public void claim() {
        if (!isReadyForClaim()) {
            throw new IllegalStateException("Cargo is not ready for claim");
        }
        
        String oldStatus = this.status.name();
        this.status = CargoStatus.CLAIMED;
        
        // Emitir evento de status
        if (eventPublisher != null) {
            eventPublisher.publish(new com.practicalddd.cargotracker.bookingms.domain.model.events.CargoStatusChangedEvent(
                this.bookingId.getBookingId(),
                oldStatus,
                this.status.name(),
                "Cargo claimed by recipient"
            ));
        }
    }

    public void markAsCompleted() {
        if (status != CargoStatus.CLAIMED) {
            throw new IllegalStateException("Only claimed cargo can be marked as completed");
        }
        
        String oldStatus = this.status.name();
        this.status = CargoStatus.COMPLETED;
        
        // Emitir evento de status
        if (eventPublisher != null) {
            eventPublisher.publish(new com.practicalddd.cargotracker.bookingms.domain.model.events.CargoStatusChangedEvent(
                this.bookingId.getBookingId(),
                oldStatus,
                this.status.name(),
                "Cargo delivery completed"
            ));
        }
    }

    public long calculateEstimatedTransitTime() {
        if (itinerary.isEmpty()) {
            return 0;
        }
        
        return itinerary.getLegs().stream()
            .mapToLong(leg -> java.time.Duration.between(leg.getLoadTime(), leg.getUnloadTime()).toHours())
            .sum();
    }

    public boolean isOnTrack() {
        if (itinerary.isEmpty()) {
            return true; // Não roteado ainda, tecnicamente está "no prazo"
        }
        
        List<Leg> legs = itinerary.getLegs();
        Leg lastLeg = legs.get(legs.size() - 1);
        LocalDateTime estimatedArrival = lastLeg.getUnloadTime();
        return !estimatedArrival.isAfter(routeSpecification.getArrivalDeadline());
    }

    private void validateItineraryTemporalFeasibility(CargoItinerary itinerary) {
        List<Leg> legs = itinerary.getLegs();
        for (int i = 0; i < legs.size(); i++) {
            Leg leg = legs.get(i);
            
            // Validar que o tempo de carga é anterior ao de descarga
            if (!leg.getLoadTime().isBefore(leg.getUnloadTime())) {
                throw new IllegalArgumentException(
                    String.format("Leg %d: Load time must be before unload time", i + 1)
                );
            }
            
            // Validar conexões entre legs
            if (i > 0) {
                Leg previousLeg = legs.get(i - 1);
                if (!previousLeg.getUnloadLocation().equals(leg.getLoadLocation())) {
                    throw new IllegalArgumentException(
                        String.format("Leg %d: Load location must match previous leg's unload location", i + 1)
                    );
                }
                // Dar um buffer de tempo mínimo entre legs
                if (previousLeg.getUnloadTime().plusHours(2).isAfter(leg.getLoadTime())) {
                    throw new IllegalArgumentException(
                        String.format("Leg %d: Insufficient time between legs (minimum 2 hours required)", i + 1)
                    );
                }
            }
        }
    }

    private void validateHandlingEvent(LastCargoHandledEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Handling event cannot be null");
        }
        
        // Validar que o evento é sequencial
        if (this.delivery.getLastEvent() != null && 
            this.delivery.getLastEvent().getHandlingEventId() != null &&
            event.getHandlingEventId() != null &&
            event.getHandlingEventId() <= this.delivery.getLastEvent().getHandlingEventId()) {
            throw new IllegalArgumentException("Handling event must have a later ID than the previous event");
        }
    }

    private void updateStatusBasedOnDelivery() {
        TransportStatus transportStatus = delivery.getTransportStatus();
        String oldStatus = this.status.name();
        
        switch (transportStatus) {
            case CLAIMED:
                this.status = CargoStatus.CLAIMED;
                break;
            case ONBOARD_CARRIER:
                this.status = CargoStatus.IN_TRANSIT;
                break;
            case IN_PORT:
                if (delivery.getLastKnownLocation().equals(routeSpecification.getDestination())) {
                    this.status = CargoStatus.READY_FOR_CLAIM;
                } else {
                    this.status = CargoStatus.IN_PORT;
                }
                break;
            default:
                // Não faz nada para outros status
                break;
        }
        
        // Emitir evento de status se mudou
        if (!oldStatus.equals(this.status.name()) && eventPublisher != null) {
            eventPublisher.publish(new com.practicalddd.cargotracker.bookingms.domain.model.events.CargoStatusChangedEvent(
                this.bookingId.getBookingId(),
                oldStatus,
                this.status.name(),
                "Status updated based on delivery progress"
            ));
        }
    }

    // Getters
    public BookingId getBookingId() { return bookingId; }
    public RouteSpecification getRouteSpecification() { return this.routeSpecification; }
    public BookingAmount getBookingAmount() { return this.bookingAmount; }
    public CargoItinerary getItinerary() { return this.itinerary; }
    public Delivery getDelivery() { return this.delivery; }
    public CargoStatus getStatus() { return this.status; }
}
