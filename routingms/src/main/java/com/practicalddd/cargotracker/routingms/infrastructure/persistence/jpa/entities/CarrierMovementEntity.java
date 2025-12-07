package com.practicalddd.cargotracker.routingms.infrastructure.persistence.jpa.entities;

import javax.persistence.*;

import com.practicalddd.cargotracker.routingms.infrastructure.persistence.jpa.converters.LocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "carrier_movement")
public class CarrierMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "voyage_number")
    private String voyageNumber;

    @Column(name = "load_location")
    private String loadLocation;

    @Column(name = "unload_location")
    private String unloadLocation;
    @Column(name = "load_time")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime loadTime;

    @Column(name = "unload_time")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime unloadTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voyage_id")
    private VoyageEntity voyage;

    // Constructors
    public CarrierMovementEntity() {
    }

    public CarrierMovementEntity(String voyageNumber, String loadLocation, String unloadLocation,
            LocalDateTime loadTime, LocalDateTime unloadTime) {
        this.voyageNumber = voyageNumber;
        this.loadLocation = loadLocation;
        this.unloadLocation = unloadLocation;
        this.loadTime = loadTime;
        this.unloadTime = unloadTime;
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

    public VoyageEntity getVoyage() {
        return voyage;
    }

    public void setVoyage(VoyageEntity voyage) {
        this.voyage = voyage;
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CarrierMovementEntity))
            return false;
        CarrierMovementEntity that = (CarrierMovementEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(voyageNumber, that.voyageNumber) &&
                Objects.equals(loadLocation, that.loadLocation) &&
                Objects.equals(unloadLocation, that.unloadLocation) &&
                Objects.equals(loadTime, that.loadTime) &&
                Objects.equals(unloadTime, that.unloadTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, voyageNumber, loadLocation, unloadLocation, loadTime, unloadTime);
    }

    // toString
    @Override
    public String toString() {
        return "CarrierMovementEntity{" +
                "id=" + id +
                ", voyageNumber='" + voyageNumber + '\'' +
                ", loadLocation='" + loadLocation + '\'' +
                ", unloadLocation='" + unloadLocation + '\'' +
                ", loadTime=" + loadTime +
                ", unloadTime=" + unloadTime +
                '}';
    }
}
