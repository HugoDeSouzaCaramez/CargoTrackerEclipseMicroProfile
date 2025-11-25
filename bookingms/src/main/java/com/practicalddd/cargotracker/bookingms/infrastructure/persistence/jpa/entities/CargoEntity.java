package com.practicalddd.cargotracker.bookingms.infrastructure.persistence.jpa.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "cargo")
@NamedQueries({
    @NamedQuery(name = "CargoEntity.findAll", query = "Select c from CargoEntity c"),
    @NamedQuery(name = "CargoEntity.findByBookingId", query = "Select c from CargoEntity c where c.bookingId = :bookingId"),
    @NamedQuery(name = "CargoEntity.getAllBookingIds", query = "Select c.bookingId from CargoEntity c")
})
public class CargoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "booking_id", unique = true)
    private String bookingId;
    
    @Column(name = "booking_amount")
    private Integer bookingAmount;
    
    @Column(name = "origin_location")
    private String originLocation;
    
    @Column(name = "dest_location")
    private String destLocation;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dest_arrival_deadline")
    private Date destArrivalDeadline;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public Integer getBookingAmount() { return bookingAmount; }
    public void setBookingAmount(Integer bookingAmount) { this.bookingAmount = bookingAmount; }
    
    public String getOriginLocation() { return originLocation; }
    public void setOriginLocation(String originLocation) { this.originLocation = originLocation; }
    
    public String getDestLocation() { return destLocation; }
    public void setDestLocation(String destLocation) { this.destLocation = destLocation; }
    
    public Date getDestArrivalDeadline() { return destArrivalDeadline; }
    public void setDestArrivalDeadline(Date destArrivalDeadline) { this.destArrivalDeadline = destArrivalDeadline; }
}