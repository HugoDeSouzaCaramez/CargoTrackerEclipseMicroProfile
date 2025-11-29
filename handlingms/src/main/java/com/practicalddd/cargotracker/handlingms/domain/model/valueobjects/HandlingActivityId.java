package com.practicalddd.cargotracker.handlingms.domain.model.valueobjects;

import java.util.Objects;

public class HandlingActivityId {
    private final Long id;

    public HandlingActivityId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HandlingActivityId)) return false;
        HandlingActivityId that = (HandlingActivityId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
