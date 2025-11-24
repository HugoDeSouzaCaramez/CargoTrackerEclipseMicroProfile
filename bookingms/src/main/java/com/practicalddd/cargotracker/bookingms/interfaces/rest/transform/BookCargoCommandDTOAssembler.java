package com.practicalddd.cargotracker.bookingms.interfaces.rest.transform;

import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.BookCargoResource;

public class BookCargoCommandDTOAssembler {

    public static BookCargoCommand toCommandFromDTO(BookCargoResource bookCargoResource){

        return new BookCargoCommand(
                                    bookCargoResource.getBookingAmount(),
                                    bookCargoResource.getOriginLocation(),
                                    bookCargoResource.getDestLocation(),
                                    java.sql.Date.valueOf(bookCargoResource.getDestArrivalDeadline()));
    }
}
