package com.practicalddd.cargotracker.bookingms.infrastructure.services.http.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement
public class TransitPathDTO implements Serializable {
    private List<TransitEdgeDTO> transitEdges;

    public TransitPathDTO() {
        this.transitEdges = new ArrayList<>();
    }

    public TransitPathDTO(List<TransitEdgeDTO> transitEdges) {
        this.transitEdges = transitEdges;
    }

    public List<TransitEdgeDTO> getTransitEdges() {
        return transitEdges;
    }

    public void setTransitEdges(List<TransitEdgeDTO> transitEdges) {
        this.transitEdges = transitEdges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransitPathDTO)) return false;
        TransitPathDTO that = (TransitPathDTO) o;
        return Objects.equals(transitEdges, that.transitEdges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transitEdges);
    }

    @Override
    public String toString() {
        return "TransitPathDTO{" +
                "transitEdges=" + transitEdges +
                '}';
    }
}
