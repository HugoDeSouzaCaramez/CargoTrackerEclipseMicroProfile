package com.practicalddd.cargotracker.bookingms.application.ports.inbound;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.commands.RouteCargoCommand;

/**
 * Porta de entrada (inbound port) para operações de roteamento de cargas.
 * Define o contrato que os adaptadores de entrada devem implementar.
 */
public interface CargoRoutingCommandPort {
    
    /**
     * Atribui uma rota a uma carga existente
     * 
     * @param routeCargoCommand Comando com o ID da carga a ser roteada
     */
    void assignRouteToCargo(RouteCargoCommand routeCargoCommand);
}
