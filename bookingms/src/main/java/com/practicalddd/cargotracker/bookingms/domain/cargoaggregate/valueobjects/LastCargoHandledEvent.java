package com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects;

public class LastCargoHandledEvent {
    private Integer handlingEventId;
    private String handlingEventType;
    private String handlingEventVoyage;
    private String handlingEventLocation;

    public static final LastCargoHandledEvent EMPTY = new LastCargoHandledEvent();

    public LastCargoHandledEvent() {}

    public LastCargoHandledEvent(Integer handlingEventId, String handlingEventType, 
                                String handlingEventVoyage, String handlingEventLocation) {
        this.handlingEventId = handlingEventId;
        this.handlingEventType = handlingEventType;
        this.handlingEventVoyage = handlingEventVoyage;
        this.handlingEventLocation = handlingEventLocation;
    }

    public String getHandlingEventType() { return this.handlingEventType; }
    public String getHandlingEventVoyage() { return this.handlingEventVoyage; }
    public Integer getHandlingEventId() { return this.handlingEventId; }
    public String getHandlingEventLocation() { return this.handlingEventLocation; }
}
