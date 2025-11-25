package com.practicalddd.cargotracker.bookingms.domain.model.repositories;

import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;

import java.util.List;
import java.util.Optional;

public interface CargoRepository {
    Optional<Cargo> find(BookingId bookingId);
    void store(Cargo cargo);
    String nextBookingId();
    List<Cargo> findAll();
    List<BookingId> findAllBookingIds();
}