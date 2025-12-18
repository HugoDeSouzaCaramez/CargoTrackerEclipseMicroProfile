package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;

import java.util.List;
import java.util.Optional;

public interface CargoRepository {
    Optional<Cargo> find(BookingId bookingId);
    void store(Cargo cargo);
    String nextBookingId();
    List<Cargo> findAll();
    List<BookingId> findAllBookingIds();
}
