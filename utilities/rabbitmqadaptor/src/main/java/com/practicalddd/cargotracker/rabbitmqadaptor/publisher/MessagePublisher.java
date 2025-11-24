package com.practicalddd.cargotracker.rabbitmqadaptor.publisher;


import com.practicalddd.cargotracker.rabbitmqadaptor.Message;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface MessagePublisher {

    void publish(Message message) throws IOException, TimeoutException;

    void publish(Message message, DeliveryOptions deliveryOptions) throws IOException, TimeoutException;

    void publish(List<Message> messages) throws IOException, TimeoutException;

    void publish(List<Message> messages, DeliveryOptions deliveryOptions) throws IOException, TimeoutException;
    
    void close() throws IOException, TimeoutException;
}
