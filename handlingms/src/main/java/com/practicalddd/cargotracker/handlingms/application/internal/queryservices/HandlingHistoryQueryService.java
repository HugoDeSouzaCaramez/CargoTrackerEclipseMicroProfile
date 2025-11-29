package com.practicalddd.cargotracker.handlingms.application.internal.queryservices;

import com.practicalddd.cargotracker.handlingms.domain.model.repositories.HandlingActivityRepository;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.CargoBookingId;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.HandlingActivityHistory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HandlingHistoryQueryService {

    @Inject
    private HandlingActivityRepository handlingActivityRepository;

    public HandlingActivityHistory getHandlingActivityHistory(String bookingId) {
        CargoBookingId cargoBookingId = new CargoBookingId(bookingId);
        return handlingActivityRepository.lookupHandlingHistoryOfCargo(cargoBookingId);
    }
}
