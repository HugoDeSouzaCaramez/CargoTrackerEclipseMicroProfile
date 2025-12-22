package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.DeadlinePolicy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * View materializada do agregado Cargo para consultas otimizadas.
 * Esta é uma projeção denormalizada do agregado Cargo.
 */
@Entity
@Table(name = "cargo_views", indexes = {
    @Index(name = "idx_cargo_view_booking_id", columnList = "booking_id", unique = true),
    @Index(name = "idx_cargo_view_status", columnList = "status"),
    @Index(name = "idx_cargo_view_origin_dest", columnList = "origin_location, destination_location"),
    @Index(name = "idx_cargo_view_deadline", columnList = "arrival_deadline"),
    @Index(name = "idx_cargo_view_last_updated", columnList = "last_updated")
})
@NamedQueries({
    @NamedQuery(name = "CargoView.findAll",
                query = "SELECT cv FROM CargoView cv ORDER BY cv.lastUpdated DESC"),
    @NamedQuery(name = "CargoView.findByBookingId",
                query = "SELECT cv FROM CargoView cv WHERE cv.bookingId = :bookingId"),
    @NamedQuery(name = "CargoView.findByStatus",
                query = "SELECT cv FROM CargoView cv WHERE cv.status = :status ORDER BY cv.lastUpdated DESC"),
    @NamedQuery(name = "CargoView.findByOrigin",
                query = "SELECT cv FROM CargoView cv WHERE cv.originLocation = :originLocation"),
    @NamedQuery(name = "CargoView.findByDestination",
                query = "SELECT cv FROM CargoView cv WHERE cv.destinationLocation = :destinationLocation"),
    @NamedQuery(name = "CargoView.findWithUpcomingDeadline",
                query = "SELECT cv FROM CargoView cv WHERE cv.arrivalDeadline BETWEEN :startDate AND :endDate ORDER BY cv.arrivalDeadline ASC"),
    @NamedQuery(name = "CargoView.findByStatuses",
                query = "SELECT cv FROM CargoView cv WHERE cv.status IN :statuses ORDER BY cv.lastUpdated DESC")
})
public class CargoView {
    
    @Id
    @Column(name = "booking_id", length = 50)
    private String bookingId;
    
    @Column(name = "booking_amount", nullable = false)
    private Integer bookingAmount;
    
    @Column(name = "origin_location", nullable = false, length = 10)
    private String originLocation;
    
    @Column(name = "destination_location", nullable = false, length = 10)
    private String destinationLocation;
    
    @Column(name = "arrival_deadline", nullable = false)
    private LocalDateTime arrivalDeadline;
    
    @Column(name = "status", nullable = false, length = 50)
    private String status;
    
    @Column(name = "routing_status", length = 50)
    private String routingStatus;
    
    @Column(name = "transport_status", length = 50)
    private String transportStatus;
    
    @Column(name = "last_known_location", length = 10)
    private String lastKnownLocation;
    
    @Column(name = "current_voyage", length = 50)
    private String currentVoyage;
    
    @Column(name = "leg_count")
    private Integer legCount;
    
    @Column(name = "estimated_transit_hours")
    private Long estimatedTransitHours;
    
    @Column(name = "is_on_track")
    private Boolean isOnTrack;
    
    @Column(name = "is_misdirected")
    private Boolean isMisdirected;
    
    @Column(name = "is_ready_for_claim")
    private Boolean isReadyForClaim;
    
    // Determina se é rota intercontinental
    @Column(name = "is_intercontinental")
    private Boolean isIntercontinental;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "version")
    private Long version;
    
    @Column(name = "aggregate_version")
    private Long aggregateVersion;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.lastUpdated = now;
        if (this.version == null) {
            this.version = 1L;
        }
        // Determinar automaticamente se é intercontinental baseado nos códigos
        if (this.isIntercontinental == null && this.originLocation != null && this.destinationLocation != null) {
            this.isIntercontinental = !originLocation.substring(0, 2).equals(destinationLocation.substring(0, 2));
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
        if (this.version != null) {
            this.version = this.version + 1;
        }
    }
    
    public CargoView() {
        // Para JPA
    }
    
    public CargoView(String bookingId, Integer bookingAmount, String originLocation, 
                    String destinationLocation, LocalDateTime arrivalDeadline, String status) {
        this.bookingId = bookingId;
        this.bookingAmount = bookingAmount;
        this.originLocation = originLocation;
        this.destinationLocation = destinationLocation;
        this.arrivalDeadline = arrivalDeadline;
        this.status = status;
        // Determinar se é intercontinental
        this.isIntercontinental = isRouteIntercontinental(originLocation, destinationLocation);
        this.onCreate();
    }
    
    private boolean isRouteIntercontinental(String originUnLocCode, String destUnLocCode) {
        if (originUnLocCode == null || destUnLocCode == null || 
            originUnLocCode.length() < 2 || destUnLocCode.length() < 2) {
            return false; // Valor padrão seguro
        }
        // Primeiras 2 letras do UN/LOCODE são o código do país
        String originCountry = originUnLocCode.substring(0, 2);
        String destCountry = destUnLocCode.substring(0, 2);
        
        return !originCountry.equals(destCountry);
    }
    
    // Método utilitário para verificar se é urgente
    public boolean isUrgent() {
        if (arrivalDeadline == null) return false;
        // Usar o construtor com o parâmetro isIntercontinental
        boolean intercontinental = isIntercontinental != null ? isIntercontinental : false;
        DeadlinePolicy policy = new DeadlinePolicy(arrivalDeadline, intercontinental);
        return policy.isUrgent();
    }
    
    // Método utilitário para verificar se está atrasado
    public boolean isDelayed() {
        if (arrivalDeadline == null || isOnTrack == null) return false;
        return !isOnTrack && arrivalDeadline.isBefore(LocalDateTime.now());
    }
    
    // Método para obter a categoria de prioridade
    public String getPriorityCategory() {
        if (arrivalDeadline == null) return "UNKNOWN";
        boolean intercontinental = isIntercontinental != null ? isIntercontinental : false;
        DeadlinePolicy policy = new DeadlinePolicy(arrivalDeadline, intercontinental);
        return policy.getPriorityCategory();
    }
    
    // Método para verificar se é crítico
    public boolean isCritical() {
        if (arrivalDeadline == null) return false;
        // Para critico, não depende se é intercontinental
        boolean intercontinental = isIntercontinental != null ? isIntercontinental : false;
        DeadlinePolicy policy = new DeadlinePolicy(arrivalDeadline, intercontinental);
        return policy.isCritical();
    }
    
    // Método para obter se é rota intercontinental
    public boolean isIntercontinental() {
        return isIntercontinental != null ? isIntercontinental : false;
    }
    
    // Getters e Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public Integer getBookingAmount() { return bookingAmount; }
    public void setBookingAmount(Integer bookingAmount) { this.bookingAmount = bookingAmount; }
    
    public String getOriginLocation() { return originLocation; }
    public void setOriginLocation(String originLocation) { 
        this.originLocation = originLocation; 
        // Atualizar isIntercontinental se ambos os locais estiverem definidos
        if (originLocation != null && destinationLocation != null) {
            this.isIntercontinental = isRouteIntercontinental(originLocation, destinationLocation);
        }
    }
    
    public String getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(String destinationLocation) { 
        this.destinationLocation = destinationLocation; 
        // Atualizar isIntercontinental se ambos os locais estiverem definidos
        if (originLocation != null && destinationLocation != null) {
            this.isIntercontinental = isRouteIntercontinental(originLocation, destinationLocation);
        }
    }
    
    public LocalDateTime getArrivalDeadline() { return arrivalDeadline; }
    public void setArrivalDeadline(LocalDateTime arrivalDeadline) { this.arrivalDeadline = arrivalDeadline; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getRoutingStatus() { return routingStatus; }
    public void setRoutingStatus(String routingStatus) { this.routingStatus = routingStatus; }
    
    public String getTransportStatus() { return transportStatus; }
    public void setTransportStatus(String transportStatus) { this.transportStatus = transportStatus; }
    
    public String getLastKnownLocation() { return lastKnownLocation; }
    public void setLastKnownLocation(String lastKnownLocation) { this.lastKnownLocation = lastKnownLocation; }
    
    public String getCurrentVoyage() { return currentVoyage; }
    public void setCurrentVoyage(String currentVoyage) { this.currentVoyage = currentVoyage; }
    
    public Integer getLegCount() { return legCount; }
    public void setLegCount(Integer legCount) { this.legCount = legCount; }
    
    public Long getEstimatedTransitHours() { return estimatedTransitHours; }
    public void setEstimatedTransitHours(Long estimatedTransitHours) { this.estimatedTransitHours = estimatedTransitHours; }
    
    public Boolean getIsOnTrack() { return isOnTrack; }
    public void setIsOnTrack(Boolean isOnTrack) { this.isOnTrack = isOnTrack; }
    
    public Boolean getIsMisdirected() { return isMisdirected; }
    public void setIsMisdirected(Boolean isMisdirected) { this.isMisdirected = isMisdirected; }
    
    public Boolean getIsReadyForClaim() { return isReadyForClaim; }
    public void setIsReadyForClaim(Boolean isReadyForClaim) { this.isReadyForClaim = isReadyForClaim; }
    
    public Boolean getIsIntercontinental() { return isIntercontinental; }
    public void setIsIntercontinental(Boolean isIntercontinental) { this.isIntercontinental = isIntercontinental; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    
    public Long getAggregateVersion() { return aggregateVersion; }
    public void setAggregateVersion(Long aggregateVersion) { this.aggregateVersion = aggregateVersion; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CargoView)) return false;
        CargoView cargoView = (CargoView) o;
        return Objects.equals(bookingId, cargoView.bookingId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }
    
    @Override
    public String toString() {
        return "CargoView{" +
                "bookingId='" + bookingId + '\'' +
                ", status='" + status + '\'' +
                ", origin='" + originLocation + '\'' +
                ", destination='" + destinationLocation + '\'' +
                ", isIntercontinental=" + isIntercontinental +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
