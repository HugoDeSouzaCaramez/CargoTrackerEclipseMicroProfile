package com.practicalddd.cargotracker.bookingms.domain.model.aggregates;

import javax.persistence.*;

import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.entities.Location;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.LastCargoHandledEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "Cargo.findAll",
                query = "Select c from Cargo c"),
        @NamedQuery(name = "Cargo.findByBookingId",
                query = "Select c from Cargo c where c.bookingId = :bookingId"),
        @NamedQuery(name = "Cargo.getAllBookingIds",
                query = "Select c.bookingId from Cargo c") })
public class Cargo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Embedded
    private BookingId bookingId;
    
    @Embedded
    private final BookingAmount bookingAmount;
    
    @Embedded
    private final RouteSpecification routeSpecification;
    
    @Embedded
    private CargoItinerary itinerary;
    
    @Embedded
    private Delivery delivery;

    public Cargo() {
        this.bookingAmount = null;
        this.routeSpecification = null;
    }

    public Cargo(BookCargoCommand bookCargoCommand) {
        this.bookingId = new BookingId(bookCargoCommand.getBookingId());
        this.routeSpecification = new RouteSpecification(
                new Location(bookCargoCommand.getOriginLocation()),
                new Location(bookCargoCommand.getDestLocation()),
                bookCargoCommand.getDestArrivalDeadline()
        );
        this.bookingAmount = new BookingAmount(bookCargoCommand.getBookingAmount());
        this.itinerary = CargoItinerary.EMPTY_ITINERARY;
        this.delivery = Delivery.derivedFrom(this.routeSpecification,
                this.itinerary, LastCargoHandledEvent.EMPTY);
    }

    public BookingId getBookingId() {
        return bookingId;
    }

    public RouteSpecification getRouteSpecification() {
        return this.routeSpecification;
    }

    public BookingAmount getBookingAmount() {
        return this.bookingAmount;
    }

    public CargoItinerary getItinerary() {
        return this.itinerary;
    }

    public Delivery getDelivery() {
        return this.delivery;
    }

    public void assignToRoute(CargoItinerary cargoItinerary) {
        if (cargoItinerary == null || cargoItinerary.getLegs().isEmpty()) {
            throw new IllegalArgumentException("Itinerary cannot be null or empty");
        }
        this.itinerary = cargoItinerary;
        this.delivery = this.delivery.updateOnRouting(this.routeSpecification, this.itinerary);
    }

    public void deriveDeliveryProgress(LastCargoHandledEvent lastCargoHandledEvent) {
        // Implementação para atualizar o progresso de entrega
        this.delivery.setLastEvent(lastCargoHandledEvent);
    }
}