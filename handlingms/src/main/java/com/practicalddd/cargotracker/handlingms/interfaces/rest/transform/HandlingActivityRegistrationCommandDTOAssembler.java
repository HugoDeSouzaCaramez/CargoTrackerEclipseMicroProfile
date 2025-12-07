package com.practicalddd.cargotracker.handlingms.interfaces.rest.transform;

import com.practicalddd.cargotracker.handlingms.domain.model.commands.HandlingActivityRegistrationCommand;
import com.practicalddd.cargotracker.handlingms.interfaces.rest.dto.HandlingActivityRegistrationResource;

public class HandlingActivityRegistrationCommandDTOAssembler {

    public static HandlingActivityRegistrationCommand toCommandFromDTO(
            HandlingActivityRegistrationResource handlingActivityRegistrationResource) {
        
        return new HandlingActivityRegistrationCommand(
                handlingActivityRegistrationResource.getBookingId(),
                handlingActivityRegistrationResource.getVoyageNumber(),
                handlingActivityRegistrationResource.getUnLocode(),
                handlingActivityRegistrationResource.getHandlingType(),
                handlingActivityRegistrationResource.getCompletionTime()
        );
    }
}
