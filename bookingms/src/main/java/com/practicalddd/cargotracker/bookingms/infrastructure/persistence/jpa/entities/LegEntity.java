package com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "leg")
public class LegEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "voyage_number")
    private String voyageNumber;
    
    @Column(name = "load_location")
    private String loadLocation;
    
    @Column(name = "unload_location")
    private String unloadLocation;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "load_time")
    private Date loadTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "unload_time")
    private Date unloadTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id")
    private CargoEntity cargo;

    public LegEntity() {}

    public LegEntity(String voyageNumber, String loadLocation, String unloadLocation, 
                    Date loadTime, Date unloadTime) {
        this.voyageNumber = voyageNumber;
        this.loadLocation = loadLocation;
        this.unloadLocation = unloadLocation;
        this.loadTime = loadTime;
        this.unloadTime = unloadTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getVoyageNumber() { return voyageNumber; }
    public void setVoyageNumber(String voyageNumber) { this.voyageNumber = voyageNumber; }
    
    public String getLoadLocation() { return loadLocation; }
    public void setLoadLocation(String loadLocation) { this.loadLocation = loadLocation; }
    
    public String getUnloadLocation() { return unloadLocation; }
    public void setUnloadLocation(String unloadLocation) { this.unloadLocation = unloadLocation; }
    
    public Date getLoadTime() { return loadTime; }
    public void setLoadTime(Date loadTime) { this.loadTime = loadTime; }
    
    public Date getUnloadTime() { return unloadTime; }
    public void setUnloadTime(Date unloadTime) { this.unloadTime = unloadTime; }
    
    public CargoEntity getCargo() { return cargo; }
    public void setCargo(CargoEntity cargo) { this.cargo = cargo; }
}