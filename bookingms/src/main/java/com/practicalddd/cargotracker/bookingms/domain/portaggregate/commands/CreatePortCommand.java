package com.practicalddd.cargotracker.bookingms.domain.portaggregate.commands;

import com.practicalddd.cargotracker.bookingms.domain.portaggregate.valueobjects.PortLocation;

public class CreatePortCommand {
    private final String unLocCode;
    private final String name;
    private final String country;
    private final String timeZone;
    private final int initialCapacity;

    public CreatePortCommand(String unLocCode, String name, String country, 
                           String timeZone, int initialCapacity) {
        this.unLocCode = unLocCode;
        this.name = name;
        this.country = country;
        this.timeZone = timeZone;
        this.initialCapacity = initialCapacity;
    }

    // Getters
    public String getUnLocCode() { return unLocCode; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getTimeZone() { return timeZone; }
    public int getInitialCapacity() { return initialCapacity; }
}
