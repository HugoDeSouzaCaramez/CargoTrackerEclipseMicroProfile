package com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "leg", indexes = {
    @Index(name = "idx_leg_load_time", columnList = "load_time"),
    @Index(name = "idx_leg_unload_time", columnList = "unload_time"),
    @Index(name = "idx_leg_voyage", columnList = "voyage_number")
})
public class LegEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "voyage_number", nullable = false, length = 50)
    private String voyageNumber;
    
    @Column(name = "load_location", nullable = false, length = 10)
    private String loadLocation;
    
    @Column(name = "unload_location", nullable = false, length = 10)
    private String unloadLocation;
    
    @Column(name = "load_time", nullable = false)
    private LocalDateTime loadTime;
    
    @Column(name = "unload_time", nullable = false)
    private LocalDateTime unloadTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_leg_cargo"))
    private CargoEntity cargo;

    public LegEntity() {}

    public LegEntity(String voyageNumber, String loadLocation, String unloadLocation, 
                    LocalDateTime loadTime, LocalDateTime unloadTime) {
        
        validateEntity(voyageNumber, loadLocation, unloadLocation, loadTime, unloadTime);
        
        this.voyageNumber = voyageNumber;
        this.loadLocation = loadLocation;
        this.unloadLocation = unloadLocation;
        this.loadTime = loadTime;
        this.unloadTime = unloadTime;
    }
    
    private void validateEntity(String voyageNumber, String loadLocation, String unloadLocation,
                               LocalDateTime loadTime, LocalDateTime unloadTime) {
        if (voyageNumber == null || voyageNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Voyage number cannot be null or empty");
        }
        if (loadLocation == null || loadLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Load location cannot be null or empty");
        }
        if (unloadLocation == null || unloadLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Unload location cannot be null or empty");
        }
        if (loadTime == null) {
            throw new IllegalArgumentException("Load time cannot be null");
        }
        if (unloadTime == null) {
            throw new IllegalArgumentException("Unload time cannot be null");
        }
        if (loadTime.isAfter(unloadTime)) {
            throw new IllegalArgumentException("Load time must be before unload time");
        }
        if (loadLocation.equals(unloadLocation)) {
            throw new IllegalArgumentException("Load and unload locations must be different");
        }
    }

    // Getters and Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getVoyageNumber() { 
        return voyageNumber; 
    }
    
    public void setVoyageNumber(String voyageNumber) { 
        this.voyageNumber = voyageNumber; 
    }
    
    public String getLoadLocation() { 
        return loadLocation; 
    }
    
    public void setLoadLocation(String loadLocation) { 
        this.loadLocation = loadLocation; 
    }
    
    public String getUnloadLocation() { 
        return unloadLocation; 
    }
    
    public void setUnloadLocation(String unloadLocation) { 
        this.unloadLocation = unloadLocation; 
    }
    
    public LocalDateTime getLoadTime() { 
        return loadTime; 
    }
    
    public void setLoadTime(LocalDateTime loadTime) { 
        this.loadTime = loadTime; 
    }
    
    public LocalDateTime getUnloadTime() { 
        return unloadTime; 
    }
    
    public void setUnloadTime(LocalDateTime unloadTime) { 
        this.unloadTime = unloadTime; 
    }
    
    public CargoEntity getCargo() { 
        return cargo; 
    }
    
    public void setCargo(CargoEntity cargo) { 
        this.cargo = cargo; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LegEntity)) return false;
        LegEntity legEntity = (LegEntity) o;
        return Objects.equals(voyageNumber, legEntity.voyageNumber) &&
               Objects.equals(loadLocation, legEntity.loadLocation) &&
               Objects.equals(unloadLocation, legEntity.unloadLocation) &&
               Objects.equals(loadTime, legEntity.loadTime) &&
               Objects.equals(unloadTime, legEntity.unloadTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voyageNumber, loadLocation, unloadLocation, loadTime, unloadTime);
    }

    @Override
    public String toString() {
        return "LegEntity{" +
                "id=" + id +
                ", voyageNumber='" + voyageNumber + '\'' +
                ", loadLocation='" + loadLocation + '\'' +
                ", unloadLocation='" + unloadLocation + '\'' +
                ", loadTime=" + loadTime +
                ", unloadTime=" + unloadTime +
                '}';
    }
}
