package com.practicalddd.cargotracker.routingms.domain.model.valueobjects;

import com.practicalddd.cargotracker.routingms.domain.model.entities.CarrierMovement;

import java.util.Collections;
import java.util.List;

public class Schedule {
    private final List<CarrierMovement> carrierMovements;

    public static final Schedule EMPTY = new Schedule();

    public Schedule() {
        this.carrierMovements = Collections.emptyList();
    }

    public Schedule(List<CarrierMovement> carrierMovements) {
        this.carrierMovements = carrierMovements != null ? 
            Collections.unmodifiableList(carrierMovements) : Collections.emptyList();
    }

    public List<CarrierMovement> getCarrierMovements() {
        return carrierMovements;
    }
}
