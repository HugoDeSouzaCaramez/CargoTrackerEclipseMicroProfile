package com.practicalddd.cargotracker.bookingms.domain.model.entities;

import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Voyage;

import java.util.Date;

public class Leg {
    private Voyage voyage;
    private Location loadLocation;
    private Location unloadLocation;
    private Date loadTime;
    private Date unloadTime;

    public Leg() {}

    public Leg(Voyage voyage, Location loadLocation,
               Location unloadLocation, Date loadTime, Date unloadTime) {
        this.voyage = voyage;
        this.loadLocation = loadLocation;
        this.unloadLocation = unloadLocation;
        this.loadTime = loadTime;
        this.unloadTime = unloadTime;
    }

    // Getters
    public Voyage getVoyage() { return voyage; }
    public Location getLoadLocation() { return loadLocation; }
    public Location getUnloadLocation() { return unloadLocation; }
    public Date getLoadTime() { return loadTime; }
    public Date getUnloadTime() { return unloadTime; }
}