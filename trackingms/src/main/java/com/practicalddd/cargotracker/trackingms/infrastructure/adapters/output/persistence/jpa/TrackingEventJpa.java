package com.practicalddd.cargotracker.trackingms.infrastructure.adapters.output.persistence.jpa;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "tracking_handling_events")
@NamedQueries({
    @NamedQuery(name = "TrackingEventJpa.findByBookingId",
            query = "Select e from TrackingEventJpa e where e.bookingId = :bookingId"),
    @NamedQuery(name = "TrackingEventJpa.findByVoyageNumber",
            query = "Select e from TrackingEventJpa e where e.voyageNumber = :voyageNumber")
})
public class TrackingEventJpa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "booking_id", nullable = false, length = 100)
    private String bookingId;
    
    @Column(name = "voyage_number", length = 50)
    private String voyageNumber;
    
    @Column(name = "location_id", length = 10)
    private String location;
    
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    
    @Column(name = "event_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;
    
    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_activity_id", insertable = false, updatable = false)
    private TrackingActivityJpa trackingActivity;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        if (this.eventTime == null) {
            this.eventTime = new Date();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }

    public TrackingEventJpa() {
        // Construtor padrão para JPA
    }

    public TrackingEventJpa(String bookingId, String voyageNumber, String location, 
                           String eventType, Date eventTime) {
        this.bookingId = bookingId;
        this.voyageNumber = voyageNumber;
        this.location = location;
        this.eventType = eventType;
        this.eventTime = eventTime != null ? new Date(eventTime.getTime()) : new Date();
    }

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

    public String getVoyageNumber() {
        return voyageNumber;
    }

    public void setVoyageNumber(String voyageNumber) {
        this.voyageNumber = voyageNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Date getEventTime() {
        return eventTime != null ? new Date(eventTime.getTime()) : null;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime != null ? new Date(eventTime.getTime()) : null;
    }

    public Date getCreatedAt() {
        return createdAt != null ? new Date(createdAt.getTime()) : null;
    }

    public Date getUpdatedAt() {
        return updatedAt != null ? new Date(updatedAt.getTime()) : null;
    }

    public TrackingActivityJpa getTrackingActivity() {
        return trackingActivity;
    }

    public void setTrackingActivity(TrackingActivityJpa trackingActivity) {
        this.trackingActivity = trackingActivity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackingEventJpa that = (TrackingEventJpa) o;
        return Objects.equals(bookingId, that.bookingId) &&
               Objects.equals(voyageNumber, that.voyageNumber) &&
               Objects.equals(location, that.location) &&
               Objects.equals(eventType, that.eventType) &&
               Objects.equals(eventTime, that.eventTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, voyageNumber, location, eventType, eventTime);
    }

    @Override
    public String toString() {
        return "TrackingEventJpa{" +
                "id=" + id +
                ", bookingId='" + bookingId + '\'' +
                ", voyageNumber='" + voyageNumber + '\'' +
                ", location='" + location + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventTime=" + eventTime +
                '}';
    }
}
