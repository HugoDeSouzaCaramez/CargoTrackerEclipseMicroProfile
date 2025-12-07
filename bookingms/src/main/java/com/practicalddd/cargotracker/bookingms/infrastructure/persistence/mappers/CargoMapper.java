package com.practicalddd.cargotracker.bookingms.infrastructure.persistence.mappers;

import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.entities.Leg;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.CargoEntity;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.LegEntity;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CargoMapper {
    
    private static final Logger logger = Logger.getLogger(CargoMapper.class.getName());
    
    public static CargoEntity toEntity(Cargo cargo) {
        if (cargo == null) {
            return null;
        }
        
        try {
            logger.fine(() -> "Mapping Cargo to Entity for booking: " + 
                cargo.getBookingId().getBookingId());
            
            CargoEntity entity = new CargoEntity();
            entity.setBookingId(cargo.getBookingId().getBookingId());
            entity.setBookingAmount(cargo.getBookingAmount().getBookingAmount());
            entity.setOriginLocation(cargo.getRouteSpecification().getOrigin().getUnLocCode());
            entity.setDestLocation(cargo.getRouteSpecification().getDestination().getUnLocCode());
            
            // ✅ Converter LocalDateTime diretamente - SEM conversão necessária
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
            
            logger.fine(() -> "Successfully mapped Cargo to Entity");
            return entity;
            
        } catch (Exception e) {
            logger.severe("Error mapping Cargo to Entity: " + e.getMessage());
            throw new RuntimeException("Mapping error: " + e.getMessage(), e);
        }
    }
    
    public static Cargo toDomain(CargoEntity entity) {
        if (entity == null) {
            return null;
        }
        
        try {
            logger.fine(() -> "Mapping Entity to Cargo for booking: " + entity.getBookingId());
            
            BookCargoCommand command = new BookCargoCommand(
                entity.getBookingId(),
                entity.getBookingAmount(),
                entity.getOriginLocation(),
                entity.getDestLocation(),
                entity.getDestArrivalDeadline()
            );
            
            Cargo cargo = new Cargo(command);
            
            // Se existirem legs na entidade, criar o itinerário
            if (entity.hasLegs()) {
                List<Leg> legs = convertLegEntitiesToDomain(entity.getLegs());
                com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary itinerary = 
                    new com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary(legs);
                cargo.assignToRoute(itinerary);
            }
            
            logger.fine(() -> "Successfully mapped Entity to Cargo");
            return cargo;
            
        } catch (Exception e) {
            logger.severe("Error mapping Entity to Cargo: " + e.getMessage());
            throw new RuntimeException("Mapping error: " + e.getMessage(), e);
        }
    }
    
    private static List<Leg> convertLegEntitiesToDomain(List<LegEntity> legEntities) {
        if (legEntities == null || legEntities.isEmpty()) {
            return Collections.emptyList();
        }
        
        return legEntities.stream()
            .map(legEntity -> new Leg(
                new com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Voyage(legEntity.getVoyageNumber()),
                new com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Location(legEntity.getLoadLocation()),
                new com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Location(legEntity.getUnloadLocation()),
                legEntity.getLoadTime(),
                legEntity.getUnloadTime()
            ))
            .collect(Collectors.toList());
    }
    
    // Método auxiliar para mapear lista de entidades
    public static List<Cargo> toDomainList(List<CargoEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        
        return entities.stream()
            .map(CargoMapper::toDomain)
            .collect(Collectors.toList());
    }
}
