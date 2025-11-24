package com.practicalddd.cargotracker.handlingms.domain.model.valueobjects;

public enum Type {

    LOAD(true),
    UNLOAD(true),
    RECEIVE(false),
    CLAIM(false),
    CUSTOMS(false);
    
    private final boolean voyageRequired;

    private Type(boolean voyageRequired) {
        this.voyageRequired = voyageRequired;
    }

    public boolean requiresVoyage() {
        return voyageRequired;
    }

    public boolean prohibitsVoyage() {
        return !requiresVoyage();
    }

    public boolean sameValueAs(Type other) {
        return other != null && this.equals(other);
    }
}