package com.practicalddd.cargotracker.routingms.domain.model.repositories;

import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.VoyageNumber;

import java.util.List;
import java.util.Optional;

public interface VoyageRepository {
    Optional<Voyage> findByVoyageNumber(VoyageNumber voyageNumber);
    List<Voyage> findAll();
    void store(Voyage voyage);
}
