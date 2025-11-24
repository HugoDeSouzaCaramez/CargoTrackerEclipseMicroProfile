package com.practicalddd.cargotracker.rabbitmqadaptor.publisher;

import com.practicalddd.cargotracker.rabbitmqadaptor.Message;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.sql.Time;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class GenericPublisher implements MessagePublisher {

    MessagePublisher publisher;

    public GenericPublisher(ConnectionFactory connectionFactory, PublisherReliability reliability) {
        if (reliability == PublisherReliability.CONFIRMED) {
            publisher = new ConfirmedPublisher(connectionFactory);
        } else if (reliability == PublisherReliability.TRANSACTIONAL) {
            publisher = new TransactionalPublisher(connectionFactory);
        } else {
            publisher = new SimplePublisher(connectionFactory);
        }
    }

    public void publish(Message message) throws IOException, TimeoutException {
        publish(message, DeliveryOptions.NONE);
    }

    public void publish(List<Message> messages) throws IOException, TimeoutException {
        publish(messages, DeliveryOptions.NONE);
    }

    public void publish(Message message, DeliveryOptions deliveryOptions)
            throws IOException, TimeoutException {
        publisher.publish(message, deliveryOptions);
    }

    public void publish(List<Message> messages, DeliveryOptions deliveryOptions) throws IOException, TimeoutException {
        publisher.publish(messages, deliveryOptions);
    }

    public void close() throws IOException, TimeoutException {
        publisher.close();
    }

}
