package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.factory;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.commands.BookCargoCommand;

public class CargoFactory {
    
    public static Cargo createCargo(BookCargoCommand bookCargoCommand, String bookingId) {
        BookCargoCommand commandWithId = new BookCargoCommand(
            bookingId,
            bookCargoCommand.getBookingAmount(),
            bookCargoCommand.getOriginLocation(),
            bookCargoCommand.getDestLocation(),
            bookCargoCommand.getDestArrivalDeadline()
        );
        
        return new Cargo(commandWithId);
    }
}
