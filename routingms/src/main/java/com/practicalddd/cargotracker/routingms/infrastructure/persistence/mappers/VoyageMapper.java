package com.practicalddd.cargotracker.routingms.infrastructure.persistence.mappers;

import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.routingms.domain.model.entities.CarrierMovement;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.Schedule;
import com.practicalddd.cargotracker.routingms.domain.model.valueobjects.VoyageNumber;
import com.practicalddd.cargotracker.routingms.infrastructure.persistence.jpa.entities.VoyageEntity;
import com.practicalddd.cargotracker.routingms.infrastructure.persistence.jpa.entities.CarrierMovementEntity;

import java.util.stream.Collectors;

public class VoyageMapper {
    
    public static Voyage toDomain(VoyageEntity entity) {
        VoyageNumber voyageNumber = new VoyageNumber(entity.getVoyageNumber());
        
        Schedule schedule = new Schedule(
            entity.getCarrierMovements().stream()
                .map(VoyageMapper::toCarrierMovementDomain)
                .collect(Collectors.toList())
        );
        
        return new Voyage(voyageNumber, schedule);
    }
    
    public static VoyageEntity toEntity(Voyage voyage) {
        VoyageEntity entity = new VoyageEntity();
        entity.setVoyageNumber(voyage.getVoyageNumber().getVoyageNumber());
        
        // Map carrier movements
        voyage.getSchedule().getCarrierMovements().forEach(movement -> {
            CarrierMovementEntity movementEntity = toCarrierMovementEntity(movement);
            entity.addCarrierMovement(movementEntity);
        });
        
        return entity;
    }
    
    private static CarrierMovement toCarrierMovementDomain(CarrierMovementEntity entity) {
        return new CarrierMovement(
            new Location(entity.getLoadLocation()),
            new Location(entity.getUnloadLocation()),
            entity.getLoadTime(),
            entity.getUnloadTime()
        );
    }
    
    private static CarrierMovementEntity toCarrierMovementEntity(CarrierMovement movement) {
        return new CarrierMovementEntity(
            null, // voyage number will be set by parent
            movement.getDepartureLocation().getUnLocCode(),
            movement.getArrivalLocation().getUnLocCode(),
            movement.getDepartureDate(),
            movement.getArrivalDate()
        );
    }
}
