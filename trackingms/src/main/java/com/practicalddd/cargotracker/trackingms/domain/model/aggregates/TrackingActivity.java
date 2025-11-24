package com.practicalddd.cargotracker.trackingms.domain.model.aggregates;

import com.practicalddd.cargotracker.trackingms.domain.model.commands.AddTrackingEventCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AssignTrackingNumberCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.entities.BookingId;
import com.practicalddd.cargotracker.trackingms.domain.model.entities.TrackingActivityEvent;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingEventType;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingLocation;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingEvent;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingVoyageNumber;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;


@Entity
@NamedQueries({
        @NamedQuery(name = "TrackingActivity.findAll",
                query = "Select t from TrackingActivity t"),
        @NamedQuery(name = "TrackingActivity.findByTrackingNumber",
                query = "Select t from TrackingActivity t where t.trackingNumber = :trackingNumber"),
        @NamedQuery(name = "TrackingActivity.getAllTrackingNos",
                query = "Select t.trackingNumber from TrackingActivity t"),
        @NamedQuery(name="TrackingActivity.findByBookingNumber",
                query = "Select t from TrackingActivity t where t.bookingId = :bookingId")})
@Table(name="tracking_activity")
public class TrackingActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private TrackingNumber trackingNumber;
    @Embedded
    private BookingId bookingId;
    @Embedded
    private TrackingActivityEvent trackingActivityEvent;

    public TrackingActivity(){}

    public TrackingActivity(AssignTrackingNumberCommand assignTrackingNumberCommand){
        this.trackingNumber = new TrackingNumber(assignTrackingNumberCommand.getTrackingNumber());
        this.bookingId = new BookingId((assignTrackingNumberCommand.getBookingId()));
        this.trackingActivityEvent = TrackingActivityEvent.EMPTY_ACTIVITY;
    }

    // método para adicionar eventos
    public void addTrackingEvent(AddTrackingEventCommand addTrackingEventCommand) {
        if (this.trackingActivityEvent == null) {
            this.trackingActivityEvent = new TrackingActivityEvent(new ArrayList<>());
        }
        
        TrackingEvent trackingEvent = new TrackingEvent(
                new TrackingVoyageNumber(addTrackingEventCommand.getVoyageNumber()),
                new TrackingLocation(addTrackingEventCommand.getLocation()),
                new TrackingEventType(addTrackingEventCommand.getEventType(), addTrackingEventCommand.getEventTime()));
        
        this.trackingActivityEvent.getTrackingEvents().add(trackingEvent);
    }

    public TrackingNumber getTrackingNumber(){
        return this.trackingNumber;
    }

    public BookingId getBookingId(){
        return this.bookingId;
    }

    public TrackingActivityEvent getTrackingActivityEvents() {
        return this.trackingActivityEvent;
    }





}
