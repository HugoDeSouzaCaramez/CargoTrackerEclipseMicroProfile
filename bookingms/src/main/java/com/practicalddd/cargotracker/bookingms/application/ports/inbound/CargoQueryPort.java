package com.practicalddd.cargotracker.bookingms.application.ports.inbound;

import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;

import java.util.List;

/**
 * Porta de entrada (inbound port) para consultas de cargas.
 * Segregação de Interface - separa operações de leitura das de escrita.
 */
public interface CargoQueryPort {
    
    /**
     * Obtém todas as cargas
     * 
     * @return Lista de todas as cargas
     */
    List<Cargo> findAllCargos();
    
    /**
     * Obtém todos os IDs de booking
     * 
     * @return Lista de IDs de booking
     */
    List<BookingId> getAllBookingIds();
    
    /**
     * Busca uma carga pelo ID de booking
     * 
     * @param bookingId ID da reserva
     * @return Carga encontrada
     * @throws RuntimeException se a carga não for encontrada
     */
    Cargo findCargoByBookingId(String bookingId);
}
