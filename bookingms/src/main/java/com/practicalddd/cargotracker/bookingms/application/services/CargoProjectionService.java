package com.practicalddd.cargotracker.bookingms.application.services;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.CargoView;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.events.*;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.CargoReadRepository;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Serviço de projeção que atualiza views materializadas de forma assíncrona
 * baseado em eventos de domínio.
 */
@ApplicationScoped
public class CargoProjectionService {
    
    private static final Logger logger = Logger.getLogger(CargoProjectionService.class.getName());
    
    @Inject
    private CargoReadRepository cargoReadRepository;
    
    @Inject
    private CargoRepository cargoRepository;
    
    /**
     * Processa evento de booking criado de forma assíncrona
     */
    @Transactional
    public void handleCargoBookedEvent(@ObservesAsync CargoBookedEvent event) {
        try {
            logger.fine(() -> "Processing CargoBookedEvent for projection: " + event.getBookingId());
            
            // Busca o agregado para obter dados completos
            Cargo cargo = cargoRepository.find(new BookingId(event.getBookingId()))
                    .orElseThrow(() -> new RuntimeException("Cargo not found for projection: " + event.getBookingId()));
            
            // Cria ou atualiza a view materializada
            CargoView view = createOrUpdateViewFromCargo(cargo);
            
            cargoReadRepository.saveView(view);
            
            logger.fine(() -> "Successfully projected CargoBookedEvent for: " + event.getBookingId());
            
        } catch (Exception e) {
            logger.severe("Error processing CargoBookedEvent for projection: " + e.getMessage());
            // Não relançar exceção para não quebrar o fluxo principal
        }
    }
    
    /**
     * Processa evento de roteamento de forma assíncrona
     */
    @Transactional
    public void handleCargoRoutedEvent(@ObservesAsync CargoRoutedEvent event) {
        try {
            logger.fine(() -> "Processing CargoRoutedEvent for projection: " + event.getBookingId());
            
            // Busca o agregado atualizado
            Cargo cargo = cargoRepository.find(new BookingId(event.getBookingId()))
                    .orElseThrow(() -> new RuntimeException("Cargo not found for projection: " + event.getBookingId()));
            
            // Atualiza a view materializada
            CargoView view = createOrUpdateViewFromCargo(cargo);
            
            cargoReadRepository.saveView(view);
            
            logger.fine(() -> "Successfully projected CargoRoutedEvent for: " + event.getBookingId());
            
        } catch (Exception e) {
            logger.severe("Error processing CargoRoutedEvent for projection: " + e.getMessage());
        }
    }
    
    /**
     * Processa evento de mudança de status de forma assíncrona
     */
    @Transactional
    public void handleCargoStatusChangedEvent(@ObservesAsync CargoStatusChangedEvent event) {
        try {
            logger.fine(() -> "Processing CargoStatusChangedEvent for projection: " + event.getBookingId() + 
                          " (" + event.getOldStatus() + " -> " + event.getNewStatus() + ")");
            
            // Busca o agregado atualizado
            Cargo cargo = cargoRepository.find(new BookingId(event.getBookingId()))
                    .orElseThrow(() -> new RuntimeException("Cargo not found for projection: " + event.getBookingId()));
            
            // Atualiza a view materializada
            CargoView view = createOrUpdateViewFromCargo(cargo);
            
            cargoReadRepository.saveView(view);
            
            logger.fine(() -> "Successfully projected CargoStatusChangedEvent for: " + event.getBookingId());
            
        } catch (Exception e) {
            logger.severe("Error processing CargoStatusChangedEvent for projection: " + e.getMessage());
        }
    }
    
    /**
     * Processa eventos de porto de forma assíncrona (se necessário)
     */
    @Transactional
    public void handlePortEvents(@ObservesAsync Object event) {
        // Esta é uma implementação genérica que pode ser estendida
        // para reagir a eventos que afetem cargos
        if (event.getClass().getSimpleName().contains("Port")) {
            logger.fine("Processing port event for projection: " + event.getClass().getSimpleName());
            
            // No futuro, atualizar views relacionadas a portos
            // ou recalcular projeções afetadas por mudanças em portos
        }
    }
    
    /**
     * Método para rebuild completo das views (útil para migrações ou recuperação)
     */
    @Transactional
    public void rebuildAllProjections() {
        try {
            logger.info("Starting full rebuild of cargo projections...");
            
            // Busca todos os cargos
            List<Cargo> allCargos = cargoRepository.findAll();
            
            logger.info("Found " + allCargos.size() + " cargos to project");
            
            int successCount = 0;
            int errorCount = 0;
            
            for (Cargo cargo : allCargos) {
                try {
                    CargoView view = createOrUpdateViewFromCargo(cargo);
                    cargoReadRepository.saveView(view);
                    successCount++;
                    
                    if (successCount % 100 == 0) {
                        logger.info("Processed " + successCount + " cargos...");
                    }
                } catch (Exception e) {
                    errorCount++;
                    logger.warning("Error projecting cargo " + cargo.getBookingId() + ": " + e.getMessage());
                }
            }
            
            logger.info("Rebuild completed. Success: " + successCount + ", Errors: " + errorCount);
            
        } catch (Exception e) {
            logger.severe("Error during full projection rebuild: " + e.getMessage());
            throw new RuntimeException("Failed to rebuild projections", e);
        }
    }
    
    /**
     * Cria ou atualiza uma view materializada a partir de um agregado Cargo
     */
    private CargoView createOrUpdateViewFromCargo(Cargo cargo) {
        CargoView view = cargoReadRepository.findViewByBookingId(cargo.getBookingId())
                .orElse(new CargoView());
        
        // Atualiza dados básicos
        view.setBookingId(cargo.getBookingId().getBookingId());
        view.setBookingAmount(cargo.getBookingAmount().getBookingAmount());
        
        if (cargo.getRouteSpecification() != null) {
            view.setOriginLocation(cargo.getRouteSpecification().getOrigin().getUnLocCode());
            view.setDestinationLocation(cargo.getRouteSpecification().getDestination().getUnLocCode());
            view.setArrivalDeadline(cargo.getRouteSpecification().getArrivalDeadline());
        }
        
        if (cargo.getStatus() != null) {
            view.setStatus(cargo.getStatus().name());
        }
        
        if (cargo.getDelivery() != null) {
            view.setRoutingStatus(cargo.getDelivery().getRoutingStatus() != null ? 
                                 cargo.getDelivery().getRoutingStatus().name() : null);
            view.setTransportStatus(cargo.getDelivery().getTransportStatus() != null ? 
                                   cargo.getDelivery().getTransportStatus().name() : null);
            view.setLastKnownLocation(cargo.getDelivery().getLastKnownLocation() != null ? 
                                     cargo.getDelivery().getLastKnownLocation().getUnLocCode() : null);
            view.setCurrentVoyage(cargo.getDelivery().getCurrentVoyage() != null ? 
                                 cargo.getDelivery().getCurrentVoyage().getVoyageNumber() : null);
        }
        
        // Calcula métricas derivadas
        view.setLegCount(cargo.getItinerary() != null ? cargo.getItinerary().getLegs().size() : 0);
        view.setEstimatedTransitHours(cargo.calculateEstimatedTransitTime());
        view.setIsOnTrack(cargo.isOnTrack());
        view.setIsMisdirected(cargo.isMisdirected());
        view.setIsReadyForClaim(cargo.isReadyForClaim());
        
        // Versão incremental
        if (view.getVersion() == null) {
            view.setVersion(1L);
        }
        
        return view;
    }
}
