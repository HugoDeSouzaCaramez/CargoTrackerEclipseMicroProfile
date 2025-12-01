package com.practicalddd.cargotracker.routingms.domain.model.aggregates;

import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Schedule;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.VoyageNumber;

public class Voyage {
    private VoyageNumber voyageNumber;
    private Schedule schedule;

    public Voyage(VoyageNumber voyageNumber, Schedule schedule) {
        this.voyageNumber = voyageNumber;
        this.schedule = schedule;
    }

    // Getters
    public VoyageNumber getVoyageNumber() { return voyageNumber; }
    public Schedule getSchedule() { return schedule; }
}
