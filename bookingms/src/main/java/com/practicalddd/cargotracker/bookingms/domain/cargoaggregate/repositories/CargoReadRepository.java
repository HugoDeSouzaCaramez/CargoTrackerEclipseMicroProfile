package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.CargoView;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId;

import java.util.List;
import java.util.Optional;

/**
 * Repositório de leitura otimizado para consultas CQRS.
 * Trabalha com views materializadas.
 */
public interface CargoReadRepository {
    
    /**
     * Busca uma view materializada pelo ID do booking
     */
    Optional<CargoView> findViewByBookingId(BookingId bookingId);
    
    /**
     * Busca uma view materializada pelo ID do booking (String)
     */
    Optional<CargoView> findViewByBookingId(String bookingId);
    
    /**
     * Retorna todas as views materializadas
     */
    List<CargoView> findAllViews();
    
    /**
     * Busca views por status
     */
    List<CargoView> findViewsByStatus(String status);
    
    /**
     * Busca views por localização de origem
     */
    List<CargoView> findViewsByOrigin(String originLocation);
    
    /**
     * Busca views por localização de destino
     */
    List<CargoView> findViewsByDestination(String destinationLocation);
    
    /**
     * Busca views com deadline próximo (para alertas)
     */
    List<CargoView> findViewsWithUpcomingDeadline(int daysAhead);
    
    /**
     * Salva ou atualiza uma view materializada
     */
    void saveView(CargoView cargoView);
    
    /**
     * Remove uma view materializada
     */
    void deleteView(BookingId bookingId);
    
    /**
     * Contagem total de views
     */
    long countAllViews();
    
    /**
     * Busca views com paginação
     */
    List<CargoView> findViewsPaginated(int page, int size);
    
    /**
     * Busca views por múltiplos status
     */
    List<CargoView> findViewsByStatuses(List<String> statuses);
    
    /**
     * Busca views por origem e destino
     */
    List<CargoView> findViewsByOriginAndDestination(String origin, String destination);
}
