package com.practicalddd.cargotracker.bookingms.domain.model.repositories;

import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;

import java.util.List;

public interface CargoRepository {
    Cargo find(BookingId bookingId);
    void store(Cargo cargo);
    String nextBookingId();
    List<Cargo> findAll();
    List<BookingId> findAllBookingIds();
}