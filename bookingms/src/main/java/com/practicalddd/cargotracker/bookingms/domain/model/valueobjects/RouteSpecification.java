package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import java.util.Date;
import java.util.Objects;

public class RouteSpecification {
    private final Location origin;
    private final Location destination;
    private final Date arrivalDeadline;

    public RouteSpecification(Location origin, Location destination, Date arrivalDeadline) {
        this.origin = origin;
        this.destination = destination;
        this.arrivalDeadline = new Date(arrivalDeadline.getTime());
    }

    public Location getOrigin() {
        return origin;
    }

    public Location getDestination() {
        return destination;
    }

    public Date getArrivalDeadline() {
        return new Date(arrivalDeadline.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteSpecification)) return false;
        RouteSpecification that = (RouteSpecification) o;
        return Objects.equals(origin, that.origin) &&
               Objects.equals(destination, that.destination) &&
               Objects.equals(arrivalDeadline, that.arrivalDeadline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination, arrivalDeadline);
    }
}