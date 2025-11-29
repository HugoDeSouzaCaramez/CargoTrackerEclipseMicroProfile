package com.practicalddd.cargotracker.handlingms.domain.model.valueobjects;

import java.util.Objects;

public class VoyageNumber {
    private final String voyageNumber;
    
    public VoyageNumber(String voyageNumber) {
        if (voyageNumber == null) {
            throw new IllegalArgumentException("VoyageNumber cannot be null");
        }
        this.voyageNumber = voyageNumber;
    }
    
    public String getVoyageNumber() {
        return this.voyageNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoyageNumber)) return false;
        VoyageNumber that = (VoyageNumber) o;
        return Objects.equals(voyageNumber, that.voyageNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voyageNumber);
    }

    @Override
    public String toString() {
        return "VoyageNumber{" +
                "voyageNumber='" + voyageNumber + '\'' +
                '}';
    }
}
