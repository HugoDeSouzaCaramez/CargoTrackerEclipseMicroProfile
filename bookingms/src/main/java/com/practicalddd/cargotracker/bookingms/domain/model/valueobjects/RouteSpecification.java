package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@Embeddable
public class RouteSpecification {
    private static final long serialVersionUID = 1L;
    
    @Embedded
    @AttributeOverride(name = "unLocCode", column = @Column(name = "spec_origin_id"))
    private final Location origin;
    
    @Embedded
    @AttributeOverride(name = "unLocCode", column = @Column(name = "spec_destination_id"))
    private final Location destination;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "spec_arrival_deadline")
    @NotNull
    private final Date arrivalDeadline;

    public RouteSpecification() {
        this.origin = null;
        this.destination = null;
        this.arrivalDeadline = null;
    }

    public RouteSpecification(Location origin, Location destination, Date arrivalDeadline) {
        this.origin = origin;
        this.destination = destination;
        this.arrivalDeadline = new Date(arrivalDeadline.getTime()); // Defensive copy
    }

    public Location getOrigin() {
        return origin;
    }

    public Location getDestination() {
        return destination;
    }

    public Date getArrivalDeadline() {
        return new Date(arrivalDeadline.getTime()); // Defensive copy
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