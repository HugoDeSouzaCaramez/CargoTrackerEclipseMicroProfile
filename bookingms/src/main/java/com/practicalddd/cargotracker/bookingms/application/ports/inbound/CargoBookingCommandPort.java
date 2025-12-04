package com.practicalddd.cargotracker.bookingms.application.ports.inbound;

import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;

/**
 * Porta de entrada (inbound port) para operações de booking de cargas.
 * Define o contrato que os adaptadores de entrada devem implementar.
 */
public interface CargoBookingCommandPort {
    
    /**
     * Reserva uma nova carga
     * 
     * @param bookCargoCommand Comando com os dados da reserva
     * @return ID da reserva criada
     */
    BookingId bookCargo(BookCargoCommand bookCargoCommand);
}
