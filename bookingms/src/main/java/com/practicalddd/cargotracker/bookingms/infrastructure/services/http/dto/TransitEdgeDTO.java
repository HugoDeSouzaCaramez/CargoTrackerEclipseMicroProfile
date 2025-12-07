package com.practicalddd.cargotracker.bookingms.infrastructure.services.http.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;  // Importando Date para conversão
import java.util.Objects;

public class TransitEdgeDTO implements Serializable {
    private String voyageNumber;
    private String fromUnLocode;
    private String toUnLocode;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    public TransitEdgeDTO() {}

    // Construtor principal com LocalDateTime
    public TransitEdgeDTO(String voyageNumber, String fromUnLocode,
                         String toUnLocode, LocalDateTime fromDate, LocalDateTime toDate) {
        this.voyageNumber = voyageNumber;
        this.fromUnLocode = fromUnLocode;
        this.toUnLocode = toUnLocode;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }
    
    // Construtor alternativo para compatibilidade com APIs que usam Date (antigo)
    public TransitEdgeDTO(String voyageNumber, String fromUnLocode,
                         String toUnLocode, Date fromDate, Date toDate) {
        this(
            voyageNumber, 
            fromUnLocode, 
            toUnLocode,
            fromDate != null ? convertToLocalDateTime(fromDate) : null,
            toDate != null ? convertToLocalDateTime(toDate) : null
        );
    }

    // Getters e Setters
    public String getVoyageNumber() { return voyageNumber; }
    public void setVoyageNumber(String voyageNumber) { this.voyageNumber = voyageNumber; }
    
    public String getFromUnLocode() { return fromUnLocode; }
    public void setFromUnLocode(String fromUnLocode) { this.fromUnLocode = fromUnLocode; }
    
    public String getToUnLocode() { return toUnLocode; }
    public void setToUnLocode(String toUnLocode) { this.toUnLocode = toUnLocode; }
    
    public LocalDateTime getFromDate() { return fromDate; }
    public void setFromDate(LocalDateTime fromDate) { this.fromDate = fromDate; }
    
    public LocalDateTime getToDate() { return toDate; }
    public void setToDate(LocalDateTime toDate) { this.toDate = toDate; }
    
    // Getters para compatibilidade com código que ainda espera Date
    public Date getFromDateAsDate() { 
        return fromDate != null ? 
            Date.from(fromDate.atZone(ZoneId.systemDefault()).toInstant()) : null; 
    }
    
    public Date getToDateAsDate() { 
        return toDate != null ? 
            Date.from(toDate.atZone(ZoneId.systemDefault()).toInstant()) : null; 
    }
    
    // Método utilitário para conversão de Date para LocalDateTime
    private static LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransitEdgeDTO)) return false;
        TransitEdgeDTO that = (TransitEdgeDTO) o;
        return Objects.equals(voyageNumber, that.voyageNumber) &&
               Objects.equals(fromUnLocode, that.fromUnLocode) &&
               Objects.equals(toUnLocode, that.toUnLocode) &&
               Objects.equals(fromDate, that.fromDate) &&
               Objects.equals(toDate, that.toDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voyageNumber, fromUnLocode, toUnLocode, fromDate, toDate);
    }

    @Override
    public String toString() {
        return "TransitEdgeDTO{" +
                "voyageNumber='" + voyageNumber + '\'' +
                ", fromUnLocode='" + fromUnLocode + '\'' +
                ", toUnLocode='" + toUnLocode + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }
}
