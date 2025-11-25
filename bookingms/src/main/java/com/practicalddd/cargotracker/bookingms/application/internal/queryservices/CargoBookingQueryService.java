package com.practicalddd.cargotracker.bookingms.application.internal.queryservices;


import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.CargoRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CargoBookingQueryService {


    @Inject
    private CargoRepository cargoRepository;

    @Transactional
    public List<Cargo> findAll(){
        return cargoRepository.findAll();
    }

   public List<BookingId> getAllBookingIds(){
       return cargoRepository.findAllBookingIds();
   }

    public Cargo find(String bookingId){
        return cargoRepository.find(new BookingId(bookingId));
    }
}
