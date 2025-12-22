package com.practicalddd.cargotracker.bookingms.infrastructure.persistence.mappers;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.CargoView;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.CargoViewResource;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CargoViewMapper {
    
    public CargoViewResource toResource(CargoView view) {
        if (view == null) return null;
        
        return new CargoViewResource(
            view.getBookingId(),
            view.getBookingAmount() != null ? view.getBookingAmount() : 0,
            view.getOriginLocation(),
            view.getDestinationLocation(),
            view.getArrivalDeadline(),
            view.getStatus(),
            view.getRoutingStatus(),
            view.getTransportStatus(),
            view.getLastKnownLocation(),
            view.getCurrentVoyage(),
            view.getLegCount() != null ? view.getLegCount() : 0,
            view.getEstimatedTransitHours() != null ? view.getEstimatedTransitHours() : 0,
            view.getIsOnTrack() != null ? view.getIsOnTrack() : false,
            view.getIsMisdirected() != null ? view.getIsMisdirected() : false,
            view.getIsReadyForClaim() != null ? view.getIsReadyForClaim() : false,
            view.isUrgent(),
            view.isDelayed(),
            view.getLastUpdated()
        );
    }
    
    public List<CargoViewResource> toResourceList(List<CargoView> views) {
        if (views == null) return Collections.emptyList();
        
        return views.stream()
                .map(this::toResource)
                .collect(Collectors.toList());
    }
}
