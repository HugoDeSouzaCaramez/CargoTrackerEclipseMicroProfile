package com.practicalddd.cargotracker.bookingms.domain.model.valueobjects;

import java.util.Objects;

public class Voyage {
    private final String voyageNumber;

    public Voyage(String voyageNumber) {
        this.voyageNumber = voyageNumber;
    }

    public String getVoyageNumber() { 
        return this.voyageNumber; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Voyage)) return false;
        Voyage voyage = (Voyage) o;
        return Objects.equals(voyageNumber, voyage.voyageNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voyageNumber);
    }
}