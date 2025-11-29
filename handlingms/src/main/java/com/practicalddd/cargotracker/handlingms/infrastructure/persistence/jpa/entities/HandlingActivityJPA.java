package com.practicalddd.cargotracker.handlingms.infrastructure.persistence.jpa.entities;

import com.practicalddd.cargotracker.handlingms.domain.model.valueobjects.Type;

import javax.persistence.*;
import java.util.Date;

@Entity
@NamedQuery(name = "HandlingActivityJPA.findByBookingId",
        query = "Select e from HandlingActivityJPA e where e.bookingId = :bookingId")
@Table(name = "handling_activity")
public class HandlingActivityJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private Type type;

    @Column(name = "voyage_number")
    private String voyageNumber;

    @Column(name = "location")
    private String location;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "event_completion_time")
    private Date completionTime;

    @Column(name = "booking_id")
    private String bookingId;

    public HandlingActivityJPA() {}

    public HandlingActivityJPA(Type type, String voyageNumber, String location, Date completionTime, String bookingId) {
        this.type = type;
        this.voyageNumber = voyageNumber;
        this.location = location;
        this.completionTime = completionTime;
        this.bookingId = bookingId;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    
    public String getVoyageNumber() { return voyageNumber; }
    public void setVoyageNumber(String voyageNumber) { this.voyageNumber = voyageNumber; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public Date getCompletionTime() { return completionTime; }
    public void setCompletionTime(Date completionTime) { this.completionTime = completionTime; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
}
