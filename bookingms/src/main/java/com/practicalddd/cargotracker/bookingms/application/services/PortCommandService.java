package com.practicalddd.cargotracker.bookingms.application.services;

import com.practicalddd.cargotracker.bookingms.domain.portaggregate.Port;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.commands.*;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.events.*;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.repositories.PortRepository;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.*;
import com.practicalddd.cargotracker.bookingms.application.events.DomainEventPublisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.logging.Logger;

@ApplicationScoped
public class PortCommandService {

    private static final Logger logger = Logger.getLogger(PortCommandService.class.getName());
    
    @Inject
    private PortRepository portRepository;
    
    @Inject
    private DomainEventPublisher eventPublisher;

    @Transactional
    public PortId createPort(CreatePortCommand command) {
        // Validar se o porto já existe
        if (portRepository.findByUnLocCode(command.getUnLocCode()).isPresent()) {
            throw new IllegalArgumentException("Port with UN/LOCODE " + command.getUnLocCode() + " already exists");
        }

        // Criar value objects
        PortId portId = new PortId(command.getUnLocCode());
        PortLocation location = new PortLocation(
            command.getUnLocCode(),
            command.getName(),
            command.getCountry(),
            command.getTimeZone()
        );
        PortCapacity capacity = new PortCapacity(0, command.getInitialCapacity());

        // Criar agregado
        Port port = new Port(portId, location, capacity);
        
        // Salvar
        portRepository.save(port);
        
        // Publicar evento
        eventPublisher.publish(new PortCreatedEvent(
            command.getUnLocCode(),
            command.getName(),
            command.getCountry(),
            command.getInitialCapacity()
        ));
        
        logger.info("Port created: " + command.getUnLocCode());
        return portId;
    }

    @Transactional
    public void updatePortCapacity(UpdatePortCapacityCommand command) {
        Port port = portRepository.findByUnLocCode(command.getPortUnLocCode())
                .orElseThrow(() -> new IllegalArgumentException("Port not found: " + command.getPortUnLocCode()));
        
        int oldCapacity = port.getCapacity().getMaxCapacity();
        
        // Atualizar capacidade
        port.updateCapacity(command.getNewMaxCapacity());
        portRepository.save(port);
        
        // Publicar evento
        eventPublisher.publish(new PortCapacityUpdatedEvent(
            command.getPortUnLocCode(),
            oldCapacity,
            command.getNewMaxCapacity()
        ));
        
        logger.info("Port capacity updated: " + command.getPortUnLocCode() + 
                   " from " + oldCapacity + " to " + command.getNewMaxCapacity());
    }

    @Transactional
    public void recordCargoMovement(RecordPortCargoMovementCommand command) {
        Port port = portRepository.findByUnLocCode(command.getPortUnLocCode())
                .orElseThrow(() -> new IllegalArgumentException("Port not found: " + command.getPortUnLocCode()));
        
        int oldUsage = port.getCapacity().getCurrentUsage();
        
        switch (command.getMovementType()) {
            case ARRIVAL:
                port.recordCargoArrival(command.getCargoAmount());
                break;
            case DEPARTURE:
                port.recordCargoDeparture(command.getCargoAmount());
                break;
            default:
                throw new IllegalArgumentException("Invalid movement type");
        }
        
        // Verificar status do porto
        port.markAsCongested();
        port.markAsOperational();
        
        portRepository.save(port);
        
        // Publicar evento
        eventPublisher.publish(new PortCargoMovementRecordedEvent(
            command.getPortUnLocCode(),
            command.getCargoAmount(),
            command.getMovementType().name(),
            port.getCapacity().getCurrentUsage(),
            port.getCapacity().getMaxCapacity()
        ));
        
        logger.info("Cargo movement recorded at port " + command.getPortUnLocCode() + 
                   ": " + command.getMovementType() + " of " + command.getCargoAmount() + " units");
    }

    public boolean canPortAccommodateCargo(String portUnLocCode, int cargoAmount) {
        return portRepository.findByUnLocCode(portUnLocCode)
                .map(port -> port.canAccommodate(cargoAmount))
                .orElse(false);
    }

    public double getPortUsagePercentage(String portUnLocCode) {
        return portRepository.findByUnLocCode(portUnLocCode)
                .map(port -> port.getCapacity().getUsagePercentage())
                .orElse(0.0);
    }
}
