package com.practicalddd.cargotracker.handlingms.domain.model.repositories;

import com.practicalddd.cargotracker.handlingms.domain.model.aggregates.HandlingActivity;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.CargoBookingId;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.HandlingActivityHistory;

public interface HandlingActivityRepository {
    void store(HandlingActivity handlingActivity);
    HandlingActivityHistory lookupHandlingHistoryOfCargo(CargoBookingId cargoBookingId);
}
