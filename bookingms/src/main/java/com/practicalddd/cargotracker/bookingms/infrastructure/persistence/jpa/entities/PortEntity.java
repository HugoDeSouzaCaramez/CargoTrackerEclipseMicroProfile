package com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "port", indexes = {
    @Index(name = "idx_port_unloccode", columnList = "unloccode", unique = true),
    @Index(name = "idx_port_country", columnList = "country"),
    @Index(name = "idx_port_status", columnList = "status")
})
@NamedQueries({
    @NamedQuery(name = "PortEntity.findByUnLocCode",
                query = "SELECT p FROM PortEntity p WHERE p.unLocCode = :unLocCode"),
    @NamedQuery(name = "PortEntity.findByCountry",
                query = "SELECT p FROM PortEntity p WHERE p.country = :country ORDER BY p.name"),
    @NamedQuery(name = "PortEntity.findCongestedPorts",
                query = "SELECT p FROM PortEntity p WHERE (p.currentUsage * 100.0 / p.maxCapacity) > 80")
})
public class PortEntity {
    
    @Id
    @Column(name = "unloccode", length = 5)
    private String unLocCode;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "country", nullable = false, length = 50)
    private String country;
    
    @Column(name = "timezone", length = 50)
    private String timeZone;
    
    @Column(name = "current_usage", nullable = false)
    private int currentUsage;
    
    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "created_at", nullable = false, updatable = false) // CORRIGIDO: updatable = false
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.lastUpdated = now;
        if (this.status == null) {
            this.status = "OPERATIONAL";
        }
        if (this.currentUsage < 0) {
            this.currentUsage = 0;
        }
        if (this.maxCapacity <= 0) {
            this.maxCapacity = 1000; // Valor padrão
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
        // Não atualiza o createdAt - ele é imutável após criação
        // Garantir que currentUsage não seja negativo
        if (this.currentUsage < 0) {
            this.currentUsage = 0;
        }
        // Garantir que maxCapacity seja positivo
        if (this.maxCapacity <= 0) {
            this.maxCapacity = 1000;
        }
        // Garantir que currentUsage não exceda maxCapacity
        if (this.currentUsage > this.maxCapacity) {
            this.currentUsage = this.maxCapacity;
        }
    }
    
    // Construtores
    public PortEntity() {}
    
    public PortEntity(String unLocCode, String name, String country, String timeZone,
                     int currentUsage, int maxCapacity, String status) {
        this.unLocCode = unLocCode;
        this.name = name;
        this.country = country;
        this.timeZone = timeZone;
        this.currentUsage = currentUsage;
        this.maxCapacity = maxCapacity;
        this.status = status;
        this.onCreate(); // Chama onCreate para inicializar datas
    }
    
    // Getters e Setters
    public String getUnLocCode() { return unLocCode; }
    public void setUnLocCode(String unLocCode) { this.unLocCode = unLocCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    
    public int getCurrentUsage() { return currentUsage; }
    public void setCurrentUsage(int currentUsage) { 
        this.currentUsage = Math.max(0, currentUsage); // Garantir não negativo
    }
    
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { 
        this.maxCapacity = Math.max(1, maxCapacity); // Garantir pelo menos 1
    }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    // REMOVER setCreatedAt() ou torná-lo privado
    private void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
    
    // Método helper
    public double getUsagePercentage() {
        return maxCapacity > 0 ? (currentUsage * 100.0 / maxCapacity) : 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PortEntity)) return false;
        PortEntity that = (PortEntity) o;
        return Objects.equals(unLocCode, that.unLocCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(unLocCode);
    }
}
