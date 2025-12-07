package com.practicalddd.cargotracker.bookingms.interfaces.rest.transform;

import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.BookCargoResource;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class BookCargoCommandDTOAssembler {

    public static BookCargoCommand toCommandFromDTO(BookCargoResource bookCargoResource) {
        if (bookCargoResource == null) {
            throw new IllegalArgumentException("BookCargoResource cannot be null");
        }
        
        validateResource(bookCargoResource);
        
        // Se getDestArrivalDeadline() retorna LocalDateTime (já tem data e hora)
        // Não precisa converter com atTime()
        LocalDateTime deadlineDateTime = bookCargoResource.getDestArrivalDeadline();
        
        return new BookCargoCommand(
            bookCargoResource.getBookingAmount(),
            bookCargoResource.getOriginLocation(),
            bookCargoResource.getDestLocation(),
            deadlineDateTime
        );
    }
    
    private static void validateResource(BookCargoResource resource) {
        if (resource.getBookingAmount() <= 0) {
            throw new IllegalArgumentException("Booking amount must be positive");
        }
        if (resource.getOriginLocation() == null || resource.getOriginLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Origin location cannot be null or empty");
        }
        if (resource.getDestLocation() == null || resource.getDestLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination location cannot be null or empty");
        }
        if (resource.getDestArrivalDeadline() == null) {
            throw new IllegalArgumentException("Arrival deadline cannot be null");
        }
        if (resource.getDestArrivalDeadline().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Arrival deadline cannot be in the past");
        }
        if (resource.getOriginLocation().equals(resource.getDestLocation())) {
            throw new IllegalArgumentException("Origin and destination must be different");
        }
    }
}
