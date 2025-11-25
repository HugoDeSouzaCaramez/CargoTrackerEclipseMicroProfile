package com.practicalddd.cargotracker.bookingms.infrastructure.persistence.mappers;

import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.entities.Leg;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.CargoEntity;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.LegEntity;

import java.util.stream.Collectors;

public class CargoMapper {
    
    public static CargoEntity toEntity(Cargo cargo) {
        CargoEntity entity = new CargoEntity();
        entity.setBookingId(cargo.getBookingId().getBookingId());
        entity.setBookingAmount(cargo.getBookingAmount().getBookingAmount());
        entity.setOriginLocation(cargo.getRouteSpecification().getOrigin().getUnLocCode());
        entity.setDestLocation(cargo.getRouteSpecification().getDestination().getUnLocCode());
        entity.setDestArrivalDeadline(cargo.getRouteSpecification().getArrivalDeadline());
        
        // Mapear legs se existirem
        if (cargo.getItinerary() != null && !cargo.getItinerary().isEmpty()) {
            cargo.getItinerary().getLegs().forEach(leg -> {
                LegEntity legEntity = new LegEntity(
                    leg.getVoyage().getVoyageNumber(),
                    leg.getLoadLocation().getUnLocCode(),
                    leg.getUnloadLocation().getUnLocCode(),
                    leg.getLoadTime(),
                    leg.getUnloadTime()
                );
                entity.addLeg(legEntity);
            });
        }
        
        return entity;
    }
    
    public static Cargo toDomain(CargoEntity entity) {
        BookCargoCommand command = new BookCargoCommand(
            entity.getBookingId(),
            entity.getBookingAmount(),
            entity.getOriginLocation(),
            entity.getDestLocation(),
            entity.getDestArrivalDeadline()
        );
        
        Cargo cargo = new Cargo(command);
        
        // Se existirem legs na entidade, criar o itinerário
        if (entity.getLegs() != null && !entity.getLegs().isEmpty()) {
            // Converter LegEntity para Leg do domínio
            java.util.List<Leg> legs = entity.getLegs().stream()
                .map(legEntity -> new Leg(
                    new com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Voyage(legEntity.getVoyageNumber()),
                    new com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Location(legEntity.getLoadLocation()),
                    new com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Location(legEntity.getUnloadLocation()),
                    legEntity.getLoadTime(),
                    legEntity.getUnloadTime()
                ))
                .collect(Collectors.toList());
            
            // Criar CargoItinerary e atribuir ao cargo
            com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary itinerary = 
                new com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary(legs);
            cargo.assignToRoute(itinerary);
        }
        
        return cargo;
    }
}