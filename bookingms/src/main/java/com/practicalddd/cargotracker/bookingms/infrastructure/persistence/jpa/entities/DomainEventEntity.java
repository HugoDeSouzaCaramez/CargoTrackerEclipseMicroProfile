package com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "domain_events", indexes = {
    @Index(name = "idx_events_aggregate", columnList = "aggregate_type, aggregate_id"),
    @Index(name = "idx_events_type", columnList = "event_type"),
    @Index(name = "idx_events_timestamp", columnList = "occurred_on")
})
@NamedQueries({
    @NamedQuery(name = "DomainEventEntity.findByAggregate",
                query = "SELECT e FROM DomainEventEntity e WHERE e.aggregateType = :aggregateType AND e.aggregateId = :aggregateId ORDER BY e.occurredOn"),
    @NamedQuery(name = "DomainEventEntity.findByType",
                query = "SELECT e FROM DomainEventEntity e WHERE e.eventType = :eventType ORDER BY e.occurredOn")
})
public class DomainEventEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;
    
    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;
    
    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData;
    
    @Column(name = "event_metadata", columnDefinition = "TEXT")
    private String eventMetadata;
    
    @Column(name = "version")
    private Integer version;
    
    @Column(name = "occurred_on", nullable = false)
    private LocalDateTime occurredOn;
    
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
    
    @PrePersist
    protected void onCreate() {
        this.recordedAt = LocalDateTime.now();
        if (this.occurredOn == null) {
            this.occurredOn = LocalDateTime.now();
        }
        if (this.version == null) {
            this.version = 1;
        }
    }
    
    // Construtores
    public DomainEventEntity() {}
    
    public DomainEventEntity(String eventType, String aggregateType, String aggregateId, 
                           String eventData, String eventMetadata, Integer version, LocalDateTime occurredOn) {
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventData = eventData;
        this.eventMetadata = eventMetadata;
        this.version = version;
        this.occurredOn = occurredOn;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    
    public String getEventData() { return eventData; }
    public void setEventData(String eventData) { this.eventData = eventData; }
    
    public String getEventMetadata() { return eventMetadata; }
    public void setEventMetadata(String eventMetadata) { this.eventMetadata = eventMetadata; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    
    public LocalDateTime getOccurredOn() { return occurredOn; }
    public void setOccurredOn(LocalDateTime occurredOn) { this.occurredOn = occurredOn; }
    
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
