package com.practicalddd.cargotracker.shareddomain.model;

import com.practicalddd.cargotracker.shareddomain.adapters.LocalDateTimeAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.LocalDateTime;

@XmlRootElement(name = "transitEdge")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransitEdge implements Serializable {

    @XmlElement(name = "voyageNumber")
    private String voyageNumber;
    
    @XmlElement(name = "fromUnLocode")
    private String fromUnLocode;
    
    @XmlElement(name = "toUnLocode")
    private String toUnLocode;
    
    @XmlElement(name = "fromDate")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime fromDate;
    
    @XmlElement(name = "toDate")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime toDate;

    public TransitEdge() {}

    public TransitEdge(String voyageNumber, String fromUnLocode,
            String toUnLocode, LocalDateTime fromDate, LocalDateTime toDate) {
        this.voyageNumber = voyageNumber;
        this.fromUnLocode = fromUnLocode;
        this.toUnLocode = toUnLocode;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public String getVoyageNumber() {
        return voyageNumber;
    }

    public void setVoyageNumber(String voyageNumber) {
        this.voyageNumber = voyageNumber;
    }

    public String getFromUnLocode() {
        return fromUnLocode;
    }

    public void setFromUnLocode(String fromUnLocode) {
        this.fromUnLocode = fromUnLocode;
    }

    public String getToUnLocode() {
        return toUnLocode;
    }

    public void setToUnLocode(String toUnLocode) {
        this.toUnLocode = toUnLocode;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDateTime getToDate() {
        return toDate;
    }

    public void setToDate(LocalDateTime toDate) {
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        return "TransitEdge{" + "voyageNumber=" + voyageNumber
                + ", fromUnLocode=" + fromUnLocode + ", toUnLocode="
                + toUnLocode + ", fromDate=" + fromDate
                + ", toDate=" + toDate + '}';
    }
}
