package com.practicalddd.cargotracker.handlingms.domain.model.aggregates;

import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.*;

import java.util.Date;

public class HandlingActivity {

    private final CargoBookingId cargoBookingId;
    private final Type type;
    private final VoyageNumber voyageNumber;
    private final Location location;
    private final Date completionTime;

    public HandlingActivity(CargoBookingId cargoBookingId, Date completionTime,
                          Type type, Location location, VoyageNumber voyageNumber) {
        
        validateHandlingActivity(type, voyageNumber);
        
        this.cargoBookingId = cargoBookingId;
        this.completionTime = (Date) completionTime.clone();
        this.type = type;
        this.location = location;
        this.voyageNumber = voyageNumber;
    }

    public HandlingActivity(CargoBookingId cargoBookingId, Date completionTime,
                          Type type, Location location) {
        
        validateHandlingActivity(type, null);
        
        this.cargoBookingId = cargoBookingId;
        this.completionTime = (Date) completionTime.clone();
        this.type = type;
        this.location = location;
        this.voyageNumber = null;
    }

    private void validateHandlingActivity(Type type, VoyageNumber voyageNumber) {
        if (type.requiresVoyage() && voyageNumber == null) {
            throw new IllegalArgumentException(
                    "VoyageNumber is required for event type " + type);
        }
        
        if (type.prohibitsVoyage() && voyageNumber != null) {
            throw new IllegalArgumentException(
                    "VoyageNumber is not allowed with event type " + type);
        }
    }

    // Business methods
    public boolean isLoadEvent() {
        return Type.LOAD.equals(this.type);
    }

    public boolean isUnloadEvent() {
        return Type.UNLOAD.equals(this.type);
    }

    // Getters apenas - sem setters para imutabilidade
    public Type getType() {
        return this.type;
    }

    public VoyageNumber getVoyage() {
        return this.voyageNumber;
    }

    public Date getCompletionTime() {
        return new Date(this.completionTime.getTime());
    }

    public Location getLocation() { 
        return this.location; 
    }

    public CargoBookingId getCargoBookingId() {
        return this.cargoBookingId;
    }
}
