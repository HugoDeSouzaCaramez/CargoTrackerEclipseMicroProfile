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
    private BookingAmount bookingAmount;
    @Embedded
    private Location origin;
    @Embedded
    private RouteSpecification routeSpecification;
    @Embedded
    private CargoItinerary itinerary;
    @Embedded
    private Delivery delivery;

    public Cargo() {}

    public Cargo(BookCargoCommand bookCargoCommand) {

        this.bookingId = new BookingId(bookCargoCommand.getBookingId());
        this.routeSpecification = new RouteSpecification(
                new Location(bookCargoCommand.getOriginLocation()),
                new Location(bookCargoCommand.getDestLocation()),
                bookCargoCommand.getDestArrivalDeadline()
        );
        this.origin = routeSpecification.getOrigin();
        this.bookingAmount = new BookingAmount(bookCargoCommand.getBookingAmount());
        this.itinerary = CargoItinerary.EMPTY_ITINERARY;
        this.delivery = Delivery.derivedFrom(this.routeSpecification,
                this.itinerary, LastCargoHandledEvent.EMPTY);
    }

    public BookingId getBookingId() {
        return bookingId;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public Location getOrigin() {
        return origin;
    }

    public RouteSpecification getRouteSpecification() {
        return this.routeSpecification;
    }


    public BookingAmount getBookingAmount(){
        return this.bookingAmount;
    }

    public void setBookingAmount(BookingAmount bookingAmount){
        this.bookingAmount = bookingAmount;
    }

    public CargoItinerary getItinerary() {
        return this.itinerary;
    }

    public void setItinerary(CargoItinerary itinerary){
        this.itinerary = itinerary;
    }


    public void assignToRoute(CargoItinerary cargoItinerary) {
        this.itinerary = cargoItinerary;
    }


    public void deriveDeliveryProgress(LastCargoHandledEvent lastCargoHandledEvent) {}


}
