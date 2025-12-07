package com.practicalddd.cargotracker.routingms.domain.model.entities;

import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Location;

import java.time.LocalDateTime;
import java.util.Objects;

public class CarrierMovement {
    private Location departureLocation;
    private Location arrivalLocation;
    private LocalDateTime departureDate;
    private LocalDateTime arrivalDate;

    public CarrierMovement() {}

    public CarrierMovement(Location departureLocation, Location arrivalLocation, 
                         LocalDateTime departureDate, LocalDateTime arrivalDate) {
        this.departureLocation = departureLocation;
        this.arrivalLocation = arrivalLocation;
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
    }

    // Getters
    public Location getDepartureLocation() { return departureLocation; }
    public Location getArrivalLocation() { return arrivalLocation; }
    public LocalDateTime getDepartureDate() { return departureDate; }
    public LocalDateTime getArrivalDate() { return arrivalDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CarrierMovement)) return false;
        CarrierMovement that = (CarrierMovement) o;
        return Objects.equals(departureLocation, that.departureLocation) &&
               Objects.equals(arrivalLocation, that.arrivalLocation) &&
               Objects.equals(departureDate, that.departureDate) &&
               Objects.equals(arrivalDate, that.arrivalDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departureLocation, arrivalLocation, departureDate, arrivalDate);
    }
}
