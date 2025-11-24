package com.practicalddd.cargotracker.rabbitmqadaptor.publisher;


import com.practicalddd.cargotracker.rabbitmqadaptor.Message;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public abstract class DiscretePublisher implements MessagePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscretePublisher.class);

    public static final int DEFAULT_RETRY_ATTEMPTS = 3;
    public static final int DEFAULT_RETRY_INTERVAL = 1000;

    private Channel channel;
    private ConnectionFactory connectionFactory;

    public DiscretePublisher(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void publish(Message message) throws IOException,TimeoutException {
        publish(message, DeliveryOptions.NONE);
    }

    public void publish(List<Message> messages) throws IOException,TimeoutException {
        publish(messages, DeliveryOptions.NONE);
    }

    public void close() throws IOException,TimeoutException {
        if (channel == null) {
            LOGGER.warn("Attempt to close a publisher channel that has not been initialized");
            return;
        } else if (! channel.isOpen()) {
            LOGGER.warn("Attempt to close a publisher channel that has already been closed or is already closing");
            return;
        }
        LOGGER.debug("Closing publisher channel");
        channel.close();
        channel = null;
        LOGGER.debug("Successfully closed publisher channel");
    }

    protected Channel provideChannel() throws IOException, TimeoutException{
        if (channel == null || !channel.isOpen()) {
            Connection connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        }
        return channel;
    }

    protected void handleIoException(int attempt, IOException ioException) throws IOException {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException e) {
                LOGGER.warn("Failed to close channel after failed publish", e);
            } catch(TimeoutException te){
                LOGGER.warn("Failed to close channel after failed publish", te);
            }
        }
        channel = null;
        if (attempt == DEFAULT_RETRY_ATTEMPTS) {
            throw ioException;
        }
        try {
            Thread.sleep(DEFAULT_RETRY_INTERVAL);
        } catch (InterruptedException e) {
            LOGGER.warn("Sending message interrupted while waiting for retry attempt", e);
        }
    }
}
