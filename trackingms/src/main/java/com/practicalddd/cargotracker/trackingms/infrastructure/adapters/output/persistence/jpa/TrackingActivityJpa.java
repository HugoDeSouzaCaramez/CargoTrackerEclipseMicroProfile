package com.practicalddd.cargotracker.trackingms.infrastructure.adapters.output.persistence.jpa;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tracking_activity")
@NamedQueries({
    @NamedQuery(name = "TrackingActivityJpa.findAll",
            query = "Select t from TrackingActivityJpa t"),
    @NamedQuery(name = "TrackingActivityJpa.findByTrackingNumber",
            query = "Select t from TrackingActivityJpa t where t.trackingNumber = :trackingNumber"),
    @NamedQuery(name = "TrackingActivityJpa.findByBookingId",
            query = "Select t from TrackingActivityJpa t where t.bookingId = :bookingId")
})
public class TrackingActivityJpa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;
    
    @Column(name = "booking_id", nullable = false, unique = true, length = 100)
    private String bookingId;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_activity_id", nullable = false)
    private List<TrackingEventJpa> trackingEvents = new ArrayList<>();

    public TrackingActivityJpa() {
        // Construtor padrão para JPA
    }

    public TrackingActivityJpa(String trackingNumber, String bookingId) {
        this.trackingNumber = trackingNumber;
        this.bookingId = bookingId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public List<TrackingEventJpa> getTrackingEvents() {
        return trackingEvents;
    }

    public void setTrackingEvents(List<TrackingEventJpa> trackingEvents) {
        this.trackingEvents.clear();
        if (trackingEvents != null) {
            this.trackingEvents.addAll(trackingEvents);
        }
    }

    public void addTrackingEvent(TrackingEventJpa trackingEvent) {
        if (trackingEvent != null) {
            this.trackingEvents.add(trackingEvent);
        }
    }

    public void removeTrackingEvent(TrackingEventJpa trackingEvent) {
        if (trackingEvent != null) {
            this.trackingEvents.remove(trackingEvent);
        }
    }

    public void clearTrackingEvents() {
        this.trackingEvents.clear();
    }

    public int getTrackingEventCount() {
        return this.trackingEvents.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackingActivityJpa that = (TrackingActivityJpa) o;
        return Objects.equals(trackingNumber, that.trackingNumber) &&
               Objects.equals(bookingId, that.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingNumber, bookingId);
    }

    @Override
    public String toString() {
        return "TrackingActivityJpa{" +
                "id=" + id +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", bookingId='" + bookingId + '\'' +
                ", trackingEventsCount=" + trackingEvents.size() +
                '}';
    }
}
