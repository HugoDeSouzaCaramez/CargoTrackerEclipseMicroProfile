package com.practicalddd.cargotracker.bookingms.application.services;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoQueryPort;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.CargoRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class CargoQueryServiceImpl implements CargoQueryPort {

    @Inject
    private CargoRepository cargoRepository;

    @Override
    public List<Cargo> findAllCargos() {
        return cargoRepository.findAll();
    }

    @Override
    public List<BookingId> getAllBookingIds() {
        return cargoRepository.findAllBookingIds();
    }

    @Override
    public Cargo findCargoByBookingId(String bookingId) {
        return cargoRepository.find(new BookingId(bookingId))
                .orElseThrow(() -> new RuntimeException("Cargo not found with ID: " + bookingId));
    }
}
