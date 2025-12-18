package com.practicalddd.cargotracker.bookingms.domain.portaggregate.repositories;

import com.practicalddd.cargotracker.bookingms.domain.portaggregate.aggregates.Port;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortId;

import java.util.List;
import java.util.Optional;

public interface PortRepository {
    Optional<Port> findById(PortId portId);
    Optional<Port> findByUnLocCode(String unLocCode);
    List<Port> findAll();
    List<Port> findByCountry(String country);
    List<Port> findCongestedPorts();
    
    void save(Port port);
    void delete(PortId portId);
    
    boolean exists(PortId portId);
}
