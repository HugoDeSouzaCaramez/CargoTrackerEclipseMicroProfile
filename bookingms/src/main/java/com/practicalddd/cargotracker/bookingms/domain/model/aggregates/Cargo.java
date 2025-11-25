package com.practicalddd.cargotracker.bookingms.domain.model.aggregates;

import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.*;

import java.util.ArrayList;
import java.util.List;

public class Cargo {
    private BookingId bookingId;
    private final BookingAmount bookingAmount;
    private final RouteSpecification routeSpecification;
    private CargoItinerary itinerary;
    private Delivery delivery;

    public Cargo(BookCargoCommand bookCargoCommand) {
        this.bookingId = new BookingId(bookCargoCommand.getBookingId());
        this.routeSpecification = new RouteSpecification(
                new Location(bookCargoCommand.getOriginLocation()),
                new Location(bookCargoCommand.getDestLocation()),
                bookCargoCommand.getDestArrivalDeadline()
        );
        this.bookingAmount = new BookingAmount(bookCargoCommand.getBookingAmount());
        this.itinerary = new CargoItinerary(new ArrayList<>());
        this.delivery = Delivery.derivedFrom(this.routeSpecification, this.itinerary, LastCargoHandledEvent.EMPTY);
    }

    public void assignToRoute(CargoItinerary cargoItinerary) {
        if (cargoItinerary == null || cargoItinerary.getLegs().isEmpty()) {
            throw new IllegalArgumentException("Itinerary cannot be null or empty");
        }
        this.itinerary = cargoItinerary;
        this.delivery = this.delivery.updateOnRouting(this.routeSpecification, this.itinerary);
    }

    public void deriveDeliveryProgress(LastCargoHandledEvent lastCargoHandledEvent) {
        this.delivery = this.delivery.updateOnHandling(lastCargoHandledEvent);
    }

    // Getters
    public BookingId getBookingId() { return bookingId; }
    public RouteSpecification getRouteSpecification() { return this.routeSpecification; }
    public BookingAmount getBookingAmount() { return this.bookingAmount; }
    public CargoItinerary getItinerary() { return this.itinerary; }
    public Delivery getDelivery() { return this.delivery; }
}
