package com.practicalddd.cargotracker.shareddomain.events;

import com.practicalddd.cargotracker.shareddomain.adapters.LocalDateTimeAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;

@XmlRootElement(name = "cargoHandledEventData")
@XmlAccessorType(XmlAccessType.FIELD)
public class CargoHandledEventData {
    
    @XmlElement(name = "bookingId")
    private String bookingId;
    
    @XmlElement(name = "handlingType")
    private String handlingType;
    
    @XmlElement(name = "handlingCompletionTime")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime handlingCompletionTime;
    
    @XmlElement(name = "handlingLocation")
    private String handlingLocation;
    
    @XmlElement(name = "voyageNumber")
    private String voyageNumber;

    public CargoHandledEventData(){}

    public CargoHandledEventData(String bookingId, String handlingType, 
                                 LocalDateTime handlingCompletionTime, 
                                 String handlingLocation, String voyageNumber){
        this.bookingId = bookingId;
        this.handlingType = handlingType;
        this.handlingCompletionTime = handlingCompletionTime;
        this.handlingLocation = handlingLocation;
        this.voyageNumber = voyageNumber;
    }

    public String getBookingId(){return this.bookingId;}
    public String getHandlingType(){return this.handlingType;}
    public LocalDateTime getHandlingCompletionTime(){return this.handlingCompletionTime;}
    public String getHandlingLocation(){return this.handlingLocation;}
    public String getVoyageNumber(){return this.voyageNumber;}

    public void setBookingId(String bookingId){this.bookingId = bookingId;}
    public void setHandlingType(String handlingType){this.handlingType = handlingType;}
    public void setHandlingCompletionTime(LocalDateTime handlingCompletionTime){this.handlingCompletionTime = handlingCompletionTime;}
    public void setHandlingLocation(String handlingLocation){this.handlingLocation = handlingLocation;}
    public void setVoyageNumber(String voyageNumber){this.voyageNumber = voyageNumber;}
    
    @Override
    public String toString() {
        return "CargoHandledEventData{" +
                "bookingId='" + bookingId + '\'' +
                ", handlingType='" + handlingType + '\'' +
                ", handlingCompletionTime=" + handlingCompletionTime +
                ", handlingLocation='" + handlingLocation + '\'' +
                ", voyageNumber='" + voyageNumber + '\'' +
                '}';
    }
}
