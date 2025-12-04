package com.practicalddd.cargotracker.bookingms.application.ports.outbound;

import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.CargoItinerary;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.RouteSpecification;

/**
 * Porta de saída (outbound port) para serviços de roteamento externo.
 * Definida pela camada de aplicação, implementada pela infraestrutura.
 */
public interface ExternalRoutingService {
    CargoItinerary fetchRouteForSpecification(RouteSpecification routeSpecification);
}
