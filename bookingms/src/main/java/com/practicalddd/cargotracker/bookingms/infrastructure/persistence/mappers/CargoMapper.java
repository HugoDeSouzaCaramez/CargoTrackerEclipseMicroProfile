package com.practicalddd.cargotracker.bookingms.infrastructure.persistence.mappers;

import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities.CargoEntity;

public class CargoMapper {
    
    public static CargoEntity toEntity(Cargo cargo) {
        CargoEntity entity = new CargoEntity();
        entity.setBookingId(cargo.getBookingId().getBookingId());
        entity.setBookingAmount(cargo.getBookingAmount().getBookingAmount());
        entity.setOriginLocation(cargo.getRouteSpecification().getOrigin().getUnLocCode());
        entity.setDestLocation(cargo.getRouteSpecification().getDestination().getUnLocCode());
        entity.setDestArrivalDeadline(cargo.getRouteSpecification().getArrivalDeadline());
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
        return new Cargo(command);
    }
}