package com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "cargo", indexes = {
    @Index(name = "idx_cargo_booking_id", columnList = "booking_id", unique = true),
    @Index(name = "idx_cargo_dest_arrival", columnList = "dest_arrival_deadline"),
    @Index(name = "idx_cargo_origin_dest", columnList = "origin_location, dest_location")
})
@NamedQueries({
    @NamedQuery(name = "CargoEntity.findAll", 
                query = "SELECT c FROM CargoEntity c ORDER BY c.destArrivalDeadline"),
    @NamedQuery(name = "CargoEntity.findByBookingId", 
                query = "SELECT c FROM CargoEntity c WHERE c.bookingId = :bookingId"),
    @NamedQuery(name = "CargoEntity.getAllBookingIds", 
                query = "SELECT c.bookingId FROM CargoEntity c ORDER BY c.bookingId"),
    @NamedQuery(name = "CargoEntity.findByOrigin", 
                query = "SELECT c FROM CargoEntity c WHERE c.originLocation = :originLocation"),
    @NamedQuery(name = "CargoEntity.findByDestination", 
                query = "SELECT c FROM CargoEntity c WHERE c.destLocation = :destLocation"),
    @NamedQuery(name = "CargoEntity.findWithDeadlineBefore", 
                query = "SELECT c FROM CargoEntity c WHERE c.destArrivalDeadline < :deadline")
})
public class CargoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "booking_id", unique = true, nullable = false, length = 50)
    private String bookingId;
    
    @Column(name = "booking_amount", nullable = false)
    private Integer bookingAmount;
    
    @Column(name = "origin_location", nullable = false, length = 10)
    private String originLocation;
    
    @Column(name = "dest_location", nullable = false, length = 10)
    private String destLocation;
    
    @Column(name = "dest_arrival_deadline", nullable = false)
    private LocalDateTime destArrivalDeadline;
    
    // Relacionamento com Legs
    @OneToMany(mappedBy = "cargo", cascade = CascadeType.ALL, orphanRemoval = true, 
               fetch = FetchType.LAZY)
    private List<LegEntity> legs = new ArrayList<>();
    
    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public CargoEntity() {
        // Para JPA
    }
    
    public CargoEntity(String bookingId, Integer bookingAmount, String originLocation, 
                      String destLocation, LocalDateTime destArrivalDeadline) {
        validateEntity(bookingId, bookingAmount, originLocation, destLocation, destArrivalDeadline);
        
        this.bookingId = bookingId;
        this.bookingAmount = bookingAmount;
        this.originLocation = originLocation;
        this.destLocation = destLocation;
        this.destArrivalDeadline = destArrivalDeadline;
    }
    
    private void validateEntity(String bookingId, Integer bookingAmount, String originLocation,
                               String destLocation, LocalDateTime destArrivalDeadline) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be null or empty");
        }
        if (bookingAmount == null || bookingAmount <= 0) {
            throw new IllegalArgumentException("Booking amount must be positive");
        }
        if (originLocation == null || originLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin location cannot be null or empty");
        }
        if (destLocation == null || destLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination location cannot be null or empty");
        }
        if (destArrivalDeadline == null) {
            throw new IllegalArgumentException("Arrival deadline cannot be null");
        }
        if (destArrivalDeadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Arrival deadline cannot be in the past");
        }
        if (originLocation.equals(destLocation)) {
            throw new IllegalArgumentException("Origin and destination must be different");
        }
    }
    
    // Getters and Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getBookingId() { 
        return bookingId; 
    }
    
    public void setBookingId(String bookingId) { 
        this.bookingId = bookingId; 
    }
    
    public Integer getBookingAmount() { 
        return bookingAmount; 
    }
    
    public void setBookingAmount(Integer bookingAmount) { 
        this.bookingAmount = bookingAmount; 
    }
    
    public String getOriginLocation() { 
        return originLocation; 
    }
    
    public void setOriginLocation(String originLocation) { 
        this.originLocation = originLocation; 
    }
    
    public String getDestLocation() { 
        return destLocation; 
    }
    
    public void setDestLocation(String destLocation) { 
        this.destLocation = destLocation; 
    }
    
    public LocalDateTime getDestArrivalDeadline() { 
        return destArrivalDeadline; 
    }
    
    public void setDestArrivalDeadline(LocalDateTime destArrivalDeadline) { 
        this.destArrivalDeadline = destArrivalDeadline; 
    }
    
    public List<LegEntity> getLegs() { 
        return legs; 
    }
    
    public void setLegs(List<LegEntity> legs) { 
        this.legs = legs; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    
    // Método helper para adicionar leg
    public void addLeg(LegEntity leg) {
        if (leg == null) {
            throw new IllegalArgumentException("Leg cannot be null");
        }
        legs.add(leg);
        leg.setCargo(this);
    }
    
    // Business method helper
    public boolean hasLegs() {
        return legs != null && !legs.isEmpty();
    }
    
    public int getLegCount() {
        return legs != null ? legs.size() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CargoEntity)) return false;
        CargoEntity that = (CargoEntity) o;
        return Objects.equals(bookingId, that.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }

    @Override
    public String toString() {
        return "CargoEntity{" +
                "id=" + id +
                ", bookingId='" + bookingId + '\'' +
                ", bookingAmount=" + bookingAmount +
                ", originLocation='" + originLocation + '\'' +
                ", destLocation='" + destLocation + '\'' +
                ", destArrivalDeadline=" + destArrivalDeadline +
                ", legCount=" + getLegCount() +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
