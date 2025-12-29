package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.specification.Specification;

import java.util.List;
import java.util.Optional;

public interface CargoRepository {
    Optional<Cargo> find(BookingId bookingId);
    void store(Cargo cargo);
    String nextBookingId();
    List<Cargo> findAll();
    
    // NOVO: Método para buscar com specification
    List<Cargo> findAll(Specification<Cargo> specification);
    
    List<BookingId> findAllBookingIds();
}
