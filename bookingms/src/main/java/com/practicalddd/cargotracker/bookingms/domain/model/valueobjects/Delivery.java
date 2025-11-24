package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import com.practicalddd.cargotracker.bookingms.domain.model.entities.Location;

import javax.persistence.*;
import java.util.Date;

@Embeddable
public class Delivery {

    public static final Date ETA_UNKOWN = null;

    @Enumerated(EnumType.STRING)
    @Column(name = "routing_status")
    private RoutingStatus routingStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_status")
    private TransportStatus transportStatus;
    @Column(name = "last_known_location_id")
    @AttributeOverride(name = "unLocCode", column = @Column(name = "last_known_location_id"))
    private Location lastKnownLocation;
    @Column(name = "current_voyage_number")
    @AttributeOverride(name = "voyageNumber", column = @Column(name = "current_voyage_number"))
    private Voyage currentVoyage;
    @Embedded
    private LastCargoHandledEvent lastEvent;

    public static final CargoHandlingActivity NO_ACTIVITY = new CargoHandlingActivity();
    @Embedded
    private CargoHandlingActivity nextExpectedActivity;


    public Delivery() {}

    public Delivery(LastCargoHandledEvent lastEvent, CargoItinerary itinerary,
                    RouteSpecification routeSpecification) {
        this.lastEvent = lastEvent;

        this.routingStatus = calculateRoutingStatus(itinerary,
                routeSpecification);
        this.transportStatus = calculateTransportStatus();
        this.lastKnownLocation = calculateLastKnownLocation();
        this.currentVoyage = calculateCurrentVoyage();
    }

    public Delivery updateOnRouting(RouteSpecification routeSpecification,
                             CargoItinerary itinerary) {


        return new Delivery(this.lastEvent, itinerary, routeSpecification);
    }

    public static Delivery derivedFrom(RouteSpecification routeSpecification,
                                CargoItinerary itinerary, LastCargoHandledEvent lastCargoHandledEvent) {

        return new Delivery(lastCargoHandledEvent, itinerary, routeSpecification);
    }


    private RoutingStatus calculateRoutingStatus(CargoItinerary itinerary,
                                                 RouteSpecification routeSpecification) {
        if (itinerary == null || itinerary == CargoItinerary.EMPTY_ITINERARY) {
            return RoutingStatus.NOT_ROUTED;
        } else {
            return RoutingStatus.ROUTED;
        }
    }

    private TransportStatus calculateTransportStatus() {
        if (lastEvent.getHandlingEventType() == null) {
            return TransportStatus.NOT_RECEIVED;
        }

        switch (lastEvent.getHandlingEventType()) {
            case "LOAD":
                return TransportStatus.ONBOARD_CARRIER;
            case "UNLOAD":
            case "RECEIVE":
            case "CUSTOMS":
                return TransportStatus.IN_PORT;
            case "CLAIM":
                return TransportStatus.CLAIMED;
            default:
                return TransportStatus.UNKNOWN;
        }
    }

    private Location calculateLastKnownLocation() {
        if (lastEvent != null) {
            return new Location(lastEvent.getHandlingEventLocation());
        } else {
            return null;
        }
    }

    private Voyage calculateCurrentVoyage() {
        if (getTransportStatus().equals(TransportStatus.ONBOARD_CARRIER) && lastEvent != null) {
            return new Voyage(lastEvent.getHandlingEventVoyage());
        } else {
            return null;
        }
    }


    public RoutingStatus getRoutingStatus() { return this.routingStatus;}
    public TransportStatus getTransportStatus() { return this.transportStatus;}
    public Location getLastKnownLocation() {
        return this.lastKnownLocation;
    }
    public void setLastKnownLocation(Location lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }
    public void setLastEvent(LastCargoHandledEvent lastEvent) {
        this.lastEvent = lastEvent;
    }
    public Voyage getCurrentVoyage() {
        return this.currentVoyage;
    }

}
