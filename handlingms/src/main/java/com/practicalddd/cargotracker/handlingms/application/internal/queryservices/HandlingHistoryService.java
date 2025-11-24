package com.practicalddd.cargotracker.handlingms.application.internal.queryservices;

import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.HandlingActivityHistory;
import com.practicalddd.cargotracker.handlingms.infrastructure.jpa.HandlingActivityRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class HandlingHistoryService {

    @Inject
    private HandlingActivityRepository handlingActivityRepository;

    @Transactional
    public HandlingActivityHistory getHandlingActivityHistory(String bookingId){
        return handlingActivityRepository.lookupHandlingHistoryOfCargo(bookingId);
    }
}
