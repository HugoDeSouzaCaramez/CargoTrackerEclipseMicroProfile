package com.practicalddd.cargotracker.bookingms.application.ports.outbound.events;

import com.practicalddd.cargotracker.rabbitmqadaptor.ContainsContent;

public class CargoRoutedIntegrationEvent implements ContainsContent<CargoRoutedEventData> {
    private CargoRoutedEventData cargoRoutedEventData;
    public void setContent(CargoRoutedEventData cargoRoutedEventData) { 
        this.cargoRoutedEventData = cargoRoutedEventData; 
    }
    public CargoRoutedEventData getContent() {
        return cargoRoutedEventData;
    }
}
