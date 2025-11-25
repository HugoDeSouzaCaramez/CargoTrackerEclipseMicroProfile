package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import com.practicalddd.cargotracker.bookingms.domain.model.entities.Leg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CargoItinerary {
    private final List<Leg> legs;

    public CargoItinerary(List<Leg> legs) {
        if (legs == null) {
            this.legs = Collections.emptyList();
        } else {
            this.legs = Collections.unmodifiableList(new ArrayList<>(legs));
        }
    }

    public List<Leg> getLegs() {
        return legs;
    }

    public boolean isEmpty() {
        return legs.isEmpty();
    }
}