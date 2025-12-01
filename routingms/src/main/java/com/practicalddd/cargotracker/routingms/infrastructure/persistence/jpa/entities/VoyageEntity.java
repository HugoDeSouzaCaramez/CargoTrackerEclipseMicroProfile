package com.practicalddd.cargotracker.routingms.infrastructure.persistence.jpa.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "voyage")
@NamedQueries({
    @NamedQuery(name = "VoyageEntity.findAll", query = "Select v from VoyageEntity v order by v.voyageNumber"),
    @NamedQuery(name = "VoyageEntity.findByVoyageNumber", query = "Select v from VoyageEntity v where v.voyageNumber = :voyageNumber")
})
public class VoyageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "voyage_number", unique = true)
    private String voyageNumber;

    @OneToMany(mappedBy = "voyage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarrierMovementEntity> carrierMovements = new ArrayList<>();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getVoyageNumber() { return voyageNumber; }
    public void setVoyageNumber(String voyageNumber) { this.voyageNumber = voyageNumber; }
    
    public List<CarrierMovementEntity> getCarrierMovements() { return carrierMovements; }
    public void setCarrierMovements(List<CarrierMovementEntity> carrierMovements) { 
        this.carrierMovements = carrierMovements; 
    }
    
    public void addCarrierMovement(CarrierMovementEntity movement) {
        carrierMovements.add(movement);
        movement.setVoyage(this);
    }
}
