package com.practicalddd.cargotracker.rabbitmqadaptor.consumer;

import com.practicalddd.cargotracker.rabbitmqadaptor.Message;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class MessageConsumer extends ConsumerContainer.ManagedConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);
    
    public void handleConsumeOk(String consumerTag) {
        LOGGER.debug("Consumer {}: Received consume OK", consumerTag);
    }

    public void handleCancelOk(String consumerTag) {
        LOGGER.debug("Consumer {}: Received cancel OK", consumerTag);
    }

    public void handleCancel(String consumerTag) throws IOException {
        LOGGER.debug("Consumer {}: Received cancel", consumerTag);
    }

    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOGGER.debug("Consumer {}: Received shutdown signal: {}", consumerTag, sig.getMessage());
    }

    public void handleRecoverOk(String consumerTag) {
        LOGGER.debug("Consumer {}: Received recover OK", consumerTag);
    }

    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
        throws IOException {
        LOGGER.debug("Consumer {}: Received handle delivery", consumerTag);
        Message message = new Message(properties)
                .exchange(envelope.getExchange())
                .routingKey(envelope.getRoutingKey())
                .deliveryTag(envelope.getDeliveryTag())
                .body(body);
        try {
            LOGGER.info("Consumer {}: Received message {}", consumerTag, envelope.getDeliveryTag());
            handleMessage(message);
        } catch (Throwable t) {
            if (!getConfiguration().isAutoAck()) {
                LOGGER.error("Consumer {}: Message {} could not be handled due to an exception during message processing", 
                    new Object[] { consumerTag, envelope.getDeliveryTag(), t });
                getChannel().basicNack(envelope.getDeliveryTag(), false, false);
                LOGGER.warn("Consumer {}: Nacked message {}",
                    new Object[] { consumerTag, envelope.getDeliveryTag(), t });
            }
            return;
        }
        if (!getConfiguration().isAutoAck()) {
            try {
                getChannel().basicAck(envelope.getDeliveryTag(), false);
                LOGGER.debug("Consumer {}: Acked message {}", consumerTag, envelope.getDeliveryTag() );
            } catch(IOException e) {
                LOGGER.error("Consumer {}: Message {} was processed but could not be acknowledged due to an exception when sending the acknowledgement", 
                    new Object[] { consumerTag, envelope.getDeliveryTag(), e });
                throw e;
            }
        }
    }

    public abstract void handleMessage(Message message);

}
