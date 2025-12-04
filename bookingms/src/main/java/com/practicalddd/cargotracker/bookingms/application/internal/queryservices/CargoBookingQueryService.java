package com.practicalddd.cargotracker.bookingms.application.internal.queryservices;

import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoQueryInboundPort;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CargoBookingQueryService implements CargoQueryInboundPort {

    @Inject
    private CargoRepository cargoRepository;

    @Override
    @Transactional
    public List<Cargo> findAllCargos(){
        return cargoRepository.findAll();
    }

    @Override
    public List<BookingId> getAllBookingIds(){
        return cargoRepository.findAllBookingIds();
    }

    @Override
    public Cargo findCargoByBookingId(String bookingId){
        return cargoRepository.find(new BookingId(bookingId))
                .orElseThrow(() -> new RuntimeException("Cargo not found"));
    }
}
