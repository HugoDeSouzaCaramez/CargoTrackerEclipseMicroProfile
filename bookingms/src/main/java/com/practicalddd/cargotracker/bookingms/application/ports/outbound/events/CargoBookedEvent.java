package com.practicalddd.cargotracker.bookingms.application.ports.outbound.events;

import com.practicalddd.cargotracker.rabbitmqadaptor.ContainsId;

public class CargoBookedEvent implements ContainsId<String> {
    private String id;
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
}
