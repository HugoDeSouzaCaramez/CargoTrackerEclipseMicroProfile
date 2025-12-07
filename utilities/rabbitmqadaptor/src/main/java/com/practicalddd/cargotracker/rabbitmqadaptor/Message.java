package com.practicalddd.cargotracker.rabbitmqadaptor;

import com.practicalddd.cargotracker.rabbitmqadaptor.publisher.DeliveryOptions;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Message {

    private final static Logger LOGGER = LoggerFactory.getLogger(Message.class);

	public static final Charset DEFAULT_MESSAGE_CHARSET = Charset.forName("UTF-8");
	public static final int DELIVERY_MODE_PERSISTENT = 2;
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_XML = "application/xml";

    private MessageReader messageReader;
    private MessageWriter messageWriter;

	private byte[] bodyContent = new byte[0];
	private BasicProperties basicProperties;
	private String routingKey = "";
	private String exchange = "";
	private long deliveryTag;

    public Message() {
        this(MessageProperties.BASIC);
    }

    public Message(BasicProperties basicProperties) {
        this.basicProperties = basicProperties;
        messageReader = new MessageReader(this);
        messageWriter = new MessageWriter(this);
    }

	public BasicProperties getBasicProperties() {
		return basicProperties;
	}

    public byte[] getBodyContent() {
        return bodyContent;
    }

    public <T> T getBodyAs(Class<T> type) {
        return messageReader.readBodyAs(type);
    }

	public String getExchange() {
		return this.exchange;
	}

	public String getRoutingKey() {
		return this.routingKey;
	}

	public long getDeliveryTag() {
		return deliveryTag;
	}

    public Message exchange(String exchange) {
        this.exchange = exchange;
        return this;
    }

    public Message queue(String queue) {
        return exchange("").routingKey(queue);
    }

    public Message routingKey(String routingKey) {
        this.routingKey = routingKey;
        return this;
    }

	public Message body(byte[] bodyContent) {
		this.bodyContent = bodyContent;
		return this;
	}

    public <T> Message body(T body) {
        messageWriter.writeBody(body);
        return this;
    }

    public <T> Message body(T body, Charset charset) {
        messageWriter.writeBody(body, charset);
        return this;
    }

	public Message persistent() {
        basicProperties = basicProperties.builder()
                .deliveryMode(DELIVERY_MODE_PERSISTENT)
                .build();
		return this;
	}

	public Message deliveryTag(long deliveryTag) {
		this.deliveryTag = deliveryTag;
		return this;
	}

    public Message contentEncoding(String charset) {
        basicProperties = basicProperties.builder()
                .contentEncoding(charset)
                .build();
        return this;
    }

    public Message contentType(String contentType) {
        basicProperties = basicProperties.builder()
                .contentType(contentType)
                .build();
        return this;
    }
    
    /**
     * Adiciona timestamp a partir de LocalDateTime
     */
    public Message timestamp(LocalDateTime dateTime) {
        Date timestamp = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        basicProperties = basicProperties.builder()
                .timestamp(timestamp)
                .build();
        return this;
    }
    
    /**
     * Adiciona timestamp atual
     */
    public Message withCurrentTimestamp() {
        Date timestamp = new Date(); // Data/hora atual
        basicProperties = basicProperties.builder()
                .timestamp(timestamp)
                .build();
        return this;
    }

    public void publish(Channel channel) throws IOException{
        publish(channel, DeliveryOptions.NONE);
    }

    public void publish(Channel channel, DeliveryOptions deliveryOptions) throws IOException {
        // Se não houver timestamp, adiciona o timestamp atual
        if (basicProperties.getTimestamp() == null) {
            Date timestamp = new Date(); // Data/hora atual como Date
            basicProperties = basicProperties.builder()
                    .timestamp(timestamp)
                    .build();
        }

        boolean mandatory = deliveryOptions == DeliveryOptions.MANDATORY;
        boolean immediate = deliveryOptions == DeliveryOptions.IMMEDIATE;

        LOGGER.info("Publishing message to exchange '{}' with routing key '{}' (deliveryOptions: {}, persistent: {})",
                new Object[] { exchange, routingKey, deliveryOptions, basicProperties.getDeliveryMode() == 2 });

        channel.basicPublish(exchange, routingKey, mandatory, immediate, basicProperties, bodyContent);
        LOGGER.info("Successfully published message to exchange '{}' with routing key '{}'", exchange, routingKey);
    }

}
