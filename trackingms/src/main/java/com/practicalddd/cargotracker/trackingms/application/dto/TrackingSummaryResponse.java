package com.practicalddd.cargotracker.trackingms.application.dto;

import javax.json.bind.annotation.JsonbProperty;
import java.util.List;

public class TrackingSummaryResponse {
    
    @JsonbProperty("totalActivities")
    private int totalActivities;
    
    @JsonbProperty("activities")
    private List<TrackingActivityResponse> activities;
    
    @JsonbProperty("page")
    private int page;
    
    @JsonbProperty("pageSize")
    private int pageSize;
    
    @JsonbProperty("totalPages")
    private int totalPages;
    
    // Construtores
    public TrackingSummaryResponse() {}
    
    public TrackingSummaryResponse(List<TrackingActivityResponse> activities, 
                                  int page, int pageSize) {
        this.activities = activities;
        this.totalActivities = activities.size();
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = calculateTotalPages();
    }
    
    private int calculateTotalPages() {
        if (pageSize <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalActivities / pageSize);
    }
    
    // Getters e Setters
    public int getTotalActivities() {
        return totalActivities;
    }
    
    public void setTotalActivities(int totalActivities) {
        this.totalActivities = totalActivities;
        this.totalPages = calculateTotalPages();
    }
    
    public List<TrackingActivityResponse> getActivities() {
        return activities;
    }
    
    public void setActivities(List<TrackingActivityResponse> activities) {
        this.activities = activities;
        this.totalActivities = activities != null ? activities.size() : 0;
        this.totalPages = calculateTotalPages();
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.totalPages = calculateTotalPages();
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    @Override
    public String toString() {
        return "TrackingSummaryResponse{" +
                "totalActivities=" + totalActivities +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                '}';
    }
}
