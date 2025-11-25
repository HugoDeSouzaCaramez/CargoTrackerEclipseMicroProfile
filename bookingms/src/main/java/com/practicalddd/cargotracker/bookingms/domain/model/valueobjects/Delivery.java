package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

public class Delivery {
    private RoutingStatus routingStatus;
    private TransportStatus transportStatus;
    private Location lastKnownLocation;
    private Voyage currentVoyage;
    private LastCargoHandledEvent lastEvent;

    public Delivery() {}

    public Delivery(LastCargoHandledEvent lastEvent, CargoItinerary itinerary,
                    RouteSpecification routeSpecification) {
        this.lastEvent = lastEvent;
        this.routingStatus = calculateRoutingStatus(itinerary, routeSpecification);
        this.transportStatus = calculateTransportStatus();
        this.lastKnownLocation = calculateLastKnownLocation();
        this.currentVoyage = calculateCurrentVoyage();
    }

    public Delivery updateOnRouting(RouteSpecification routeSpecification, CargoItinerary itinerary) {
        return new Delivery(this.lastEvent, itinerary, routeSpecification);
    }

    public Delivery updateOnHandling(LastCargoHandledEvent lastCargoHandledEvent) {
        Delivery newDelivery = new Delivery(lastCargoHandledEvent, null, null);
        newDelivery.routingStatus = this.routingStatus;
        return newDelivery;
    }

    public static Delivery derivedFrom(RouteSpecification routeSpecification,
                                CargoItinerary itinerary, LastCargoHandledEvent lastCargoHandledEvent) {
        return new Delivery(lastCargoHandledEvent, itinerary, routeSpecification);
    }

    private RoutingStatus calculateRoutingStatus(CargoItinerary itinerary, RouteSpecification routeSpecification) {
        if (itinerary == null || itinerary.isEmpty()) {
            return RoutingStatus.NOT_ROUTED;
        } else {
            return RoutingStatus.ROUTED;
        }
    }

    private TransportStatus calculateTransportStatus() {
        if (lastEvent == null || lastEvent.getHandlingEventType() == null) {
            return TransportStatus.NOT_RECEIVED;
        }

        switch (lastEvent.getHandlingEventType()) {
            case "LOAD": return TransportStatus.ONBOARD_CARRIER;
            case "UNLOAD":
            case "RECEIVE":
            case "CUSTOMS": return TransportStatus.IN_PORT;
            case "CLAIM": return TransportStatus.CLAIMED;
            default: return TransportStatus.UNKNOWN;
        }
    }

    private Location calculateLastKnownLocation() {
        if (lastEvent != null && lastEvent.getHandlingEventLocation() != null) {
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

    // Getters
    public RoutingStatus getRoutingStatus() { return this.routingStatus; }
    public TransportStatus getTransportStatus() { return this.transportStatus; }
    public Location getLastKnownLocation() { return this.lastKnownLocation; }
    public Voyage getCurrentVoyage() { return this.currentVoyage; }
    public LastCargoHandledEvent getLastEvent() { return this.lastEvent; }
}