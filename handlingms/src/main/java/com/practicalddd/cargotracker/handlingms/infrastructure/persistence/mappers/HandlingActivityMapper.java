package com.practicalddd.cargotracker.handlingms.infrastructure.persistence.mappers;

import com.practicalddd.cargotracker.handlingms.domain.model.aggregates.HandlingActivity;
import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.*;
import com.practicalddd.cargotracker.handlingms.infrastructure.persistence.jpa.entities.HandlingActivityJPA;

import java.util.logging.Logger;

public class HandlingActivityMapper {
    
    private static final Logger logger = Logger.getLogger(HandlingActivityMapper.class.getName());

    public static HandlingActivityJPA toJPA(HandlingActivity handlingActivity) {
        if (handlingActivity == null) {
            return null;
        }
        
        try {
            HandlingActivityJPA jpa = new HandlingActivityJPA();
            
            jpa.setBookingId(handlingActivity.getCargoBookingId().getBookingId());
            jpa.setCompletionTime(handlingActivity.getCompletionTime());
            jpa.setType(handlingActivity.getType());
            jpa.setLocation(handlingActivity.getLocation().getUnLocCode());
            
            if (handlingActivity.getVoyage() != null) {
                jpa.setVoyageNumber(handlingActivity.getVoyage().getVoyageNumber());
            }
            
            logger.info("Mapped HandlingActivity to JPA for booking: " + handlingActivity.getCargoBookingId().getBookingId());
            return jpa;
        } catch (Exception e) {
            logger.severe("Error mapping HandlingActivity to JPA: " + e.getMessage());
            throw new RuntimeException("Mapping error: " + e.getMessage(), e);
        }
    }

    public static HandlingActivity toDomain(HandlingActivityJPA jpa) {
        if (jpa == null) {
            return null;
        }
        
        try {
            CargoBookingId cargoBookingId = new CargoBookingId(jpa.getBookingId());
            Location location = new Location(jpa.getLocation());
            
            if (jpa.getVoyageNumber() != null && !jpa.getVoyageNumber().isEmpty()) {
                VoyageNumber voyageNumber = new VoyageNumber(jpa.getVoyageNumber());
                return new HandlingActivity(cargoBookingId, jpa.getCompletionTime(), 
                                         jpa.getType(), location, voyageNumber);
            } else {
                return new HandlingActivity(cargoBookingId, jpa.getCompletionTime(), 
                                         jpa.getType(), location);
            }
        } catch (Exception e) {
            logger.severe("Error mapping JPA to HandlingActivity: " + e.getMessage());
            throw new RuntimeException("Mapping error: " + e.getMessage(), e);
        }
    }
}
