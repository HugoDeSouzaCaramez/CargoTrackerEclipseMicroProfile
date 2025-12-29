package com.practicalddd.cargotracker.bookingms.domain.services;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.RouteSpecification;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.Port;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.repositories.PortRepository;
import com.practicalddd.cargotracker.bookingms.domain.portaggregate.specification.PortDomainSpecification;
import com.practicalddd.cargotracker.bookingms.domain.specification.Specification;
import com.practicalddd.cargotracker.bookingms.domain.specification.Specifications;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Domain Service para operações de roteamento que envolvem múltiplos agregados
 */
@ApplicationScoped
public class CargoRoutingService {

    @Inject
    private PortRepository portRepository;

    @Inject
    private CargoPortValidationService validationService;

    /**
     * Encontra portos adequados para uma especificação de rota
     */
    public List<Port> findSuitablePortsForRoute(RouteSpecification routeSpec, int cargoAmount) {
        // Usando Specification Pattern para buscar portos (domínio)
        Specification<Port> spec = PortDomainSpecification.operationalPorts();
        spec = Specifications.and(spec, PortDomainSpecification.withAvailableCapacity(cargoAmount));

        return portRepository.findAll(spec);
    }

    /**
     * Verifica se todos os portos em uma rota podem acomodar a carga
     */
    public boolean canRouteAccommodateCargo(Cargo cargo, List<String> portUnLocCodes) {
        if (cargo == null || portUnLocCodes == null || portUnLocCodes.isEmpty()) {
            return false;
        }

        int requiredAmount = cargo.getBookingAmount().getBookingAmount();
        return portUnLocCodes.stream()
                .allMatch(portCode -> validationService.validatePortCapacity(portCode, requiredAmount).isValid());
    }

    /**
     * Sugere portos alternativos baseado em congestionamento
     */
    public List<Port> suggestAlternativePorts(String originalPortUnLocCode, int cargoAmount, String region) {
        // Busca portos na mesma região que não estejam congestionados
        Specification<Port> spec = Specifications.and(
                PortDomainSpecification.byCountry(region), // Simplificado: usando país como região
                Specifications.and(
                        Specifications.not(PortDomainSpecification.congestedPorts()),
                        PortDomainSpecification.withAvailableCapacity(cargoAmount)));

        return portRepository.findAll(spec);
    }
}
