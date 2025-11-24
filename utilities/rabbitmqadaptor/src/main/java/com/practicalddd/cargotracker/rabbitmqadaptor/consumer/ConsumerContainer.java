package com.practicalddd.cargotracker.rabbitmqadaptor.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.annotation.PostConstruct;

import com.practicalddd.cargotracker.rabbitmqadaptor.ConnectionListener;
import com.practicalddd.cargotracker.rabbitmqadaptor.SingleConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ShutdownListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Time;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class ConsumerContainer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerContainer.class);
    private static final int DEFAULT_AMOUNT_OF_INSTANCES = 1;

    @Inject
    ConnectionFactory connectionFactory;
    
    List<ConsumerHolder> consumerHolders = Collections.synchronizedList(new LinkedList<ConsumerHolder>());
    
    private final Object activationMonitor = new Object();
    
    public ConsumerContainer() {
        LOGGER.debug("ConsumerContainer instanciado pelo CDI");
    }
    
    public ConsumerContainer(ConnectionFactory connectionFactory) {
        super();
        this.connectionFactory = connectionFactory;
        if (connectionFactory instanceof SingleConnectionFactory) {
            ContainerConnectionListener connectionListener = new ContainerConnectionListener();
            ((SingleConnectionFactory)connectionFactory).registerListener(connectionListener);
        }
    }

    @PostConstruct
    public void initialize() {
        LOGGER.info("ConsumerContainer inicializado com ConnectionFactory: {}", connectionFactory);
        if (connectionFactory instanceof SingleConnectionFactory) {
            ContainerConnectionListener connectionListener = new ContainerConnectionListener();
            ((SingleConnectionFactory)connectionFactory).registerListener(connectionListener);
            LOGGER.debug("ContainerConnectionListener registrado");
        }
    }

    public void addConsumer(Consumer consumer, String queue) {
        addConsumer(consumer, new ConsumerConfiguration(queue), DEFAULT_AMOUNT_OF_INSTANCES);
    }

    public void addConsumer(Consumer consumer, String queue, boolean autoAck) {
        addConsumer(consumer, new ConsumerConfiguration(queue, autoAck), DEFAULT_AMOUNT_OF_INSTANCES);
    }

    public void addConsumer(Consumer consumer, String queue, int prefetchMessageCount, int instances) {
        addConsumer(consumer, new ConsumerConfiguration(queue, prefetchMessageCount), instances);
    }

    public void addConsumer(Consumer consumer, String queue, int instances) {
        addConsumer(consumer, new ConsumerConfiguration(queue), instances);
    }

    public void addConsumer(Consumer consumer, String queue, boolean autoAck, int prefetchMessageCount, int instances) {
        addConsumer(consumer, new ConsumerConfiguration(queue, autoAck, prefetchMessageCount), instances);
    }

    public void addConsumer(Consumer consumer, String queue, boolean autoAck, int instances) {
        addConsumer(consumer, new ConsumerConfiguration(queue, autoAck), instances);
    }

    public synchronized void addConsumer(Consumer consumer, ConsumerConfiguration configuration, int instances) {
        for (int i=0; i < instances; i++) {
            this.consumerHolders.add(new ConsumerHolder(consumer, configuration));
        }
    }

    public synchronized void startConsumers(Class<? extends Consumer> consumerClass) throws IOException,TimeoutException {
        List<ConsumerHolder> consumerHolderSubList = filterConsumersForClass(consumerClass);
        enableConsumers(consumerHolderSubList);
    }

    public synchronized void startAllConsumers() throws IOException,TimeoutException {
        enableConsumers(consumerHolders);
    }

    public synchronized void stopConsumers(Class<? extends Consumer> consumerClass) {
        List<ConsumerHolder> consumerHolderSubList = filterConsumersForClass(consumerClass);
        disableConsumers(consumerHolderSubList);
    }

    public synchronized void stopAllConsumers() {
        disableConsumers(consumerHolders);
    }

    public synchronized void reset() {
        disableConsumers(consumerHolders);
        consumerHolders.clear();
    }
    
    public List<ConsumerHolder> getEnabledConsumers() {
        return filterConsumersForEnabledFlag(true);
    }
    
    public List<ConsumerHolder> getDisabledConsumers() {
        return filterConsumersForEnabledFlag(false);
    }
    
    public List<ConsumerHolder> getActiveConsumers() {
        return filterConsumersForActiveFlag(true);
    }
    
    public List<ConsumerHolder> getInactiveConsumers() {
        return filterConsumersForActiveFlag(false);
    }
    
    protected List<ConsumerHolder> filterConsumersForClass(Class<? extends Consumer> consumerClass) {
        List<ConsumerHolder> consumerHolderSubList = new LinkedList<ConsumerHolder>();
        for (ConsumerHolder consumerHolder : consumerHolders) {
            if (consumerClass.isAssignableFrom(consumerHolder.getConsumer().getClass())) {
                consumerHolderSubList.add(consumerHolder);
            }
        }
        return consumerHolderSubList;
    }
    
    protected List<ConsumerHolder> filterConsumersForEnabledFlag(boolean enabled) {
        List<ConsumerHolder> consumerHolderSubList = new LinkedList<ConsumerHolder>();
        for (ConsumerHolder consumerHolder : consumerHolders) {
            if (consumerHolder.isEnabled() == enabled) {
                consumerHolderSubList.add(consumerHolder);
            }
        }
        return consumerHolderSubList;
    }
    
    protected List<ConsumerHolder> filterConsumersForActiveFlag(boolean active) {
        List<ConsumerHolder> consumerHolderSubList = new LinkedList<ConsumerHolder>();
        for (ConsumerHolder consumerHolder : consumerHolders) {
            if (consumerHolder.isActive() == active) {
                consumerHolderSubList.add(consumerHolder);
            }
        }
        return consumerHolderSubList;
    }
    
    protected void enableConsumers(List<ConsumerHolder> consumerHolders) throws IOException,TimeoutException {
        checkPreconditions(consumerHolders);
        try {
            for (ConsumerHolder consumerHolder : consumerHolders) {
                consumerHolder.enable();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to enable consumers - disabling already enabled consumers");
            disableConsumers(consumerHolders);
            throw e;
        }
    }
    
    protected void disableConsumers(List<ConsumerHolder> consumerHolders) {
        for (ConsumerHolder consumerHolder : consumerHolders) {
            consumerHolder.disable();
        }
    }
    
    protected void activateConsumers(List<ConsumerHolder> consumerHolders) throws IOException,TimeoutException {
        synchronized (activationMonitor) {
            for (ConsumerHolder consumerHolder : consumerHolders) {
                try {
                    consumerHolder.activate();
                } catch (IOException e) {
                    LOGGER.error("Failed to activate consumer - deactivating already activated consumers");
                    deactivateConsumers(consumerHolders);
                    throw e;
                }
            }
        }
    }
    
    protected void deactivateConsumers(List<ConsumerHolder> consumerHolders) {
        synchronized (activationMonitor) {
            for (ConsumerHolder consumerHolder : consumerHolders) {
                consumerHolder.deactivate();
            }
        }
    }
    
    protected void checkPreconditions(List<ConsumerHolder> consumerHolders) throws IOException, TimeoutException {
        Channel channel = createChannel();
        for (ConsumerHolder consumerHolder : consumerHolders) {
            String queue = consumerHolder.getConfiguration().getQueueName();
            try {
                channel.queueDeclare(queue, true, false, false, null);
                LOGGER.debug("Queue {} found on broker", queue);
            } catch (IOException e) {
                LOGGER.error("Queue {} not found on broker", queue);
                throw e;
            }
        }
        channel.close();
    }

    protected Channel createChannel() throws IOException, TimeoutException {
        LOGGER.debug("Creating channel");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        LOGGER.debug("Created channel");
        return channel;
    }
    
    protected class ContainerConnectionListener implements ConnectionListener {
        

        public void onConnectionEstablished(Connection connection) {
            String hostName = connection.getAddress().getHostName();
            LOGGER.info("Connection established to {}", hostName);
            List<ConsumerHolder> enabledConsumerHolders = filterConsumersForEnabledFlag(true);
            LOGGER.info("Activating {} enabled consumers", enabledConsumerHolders.size());
            try {
                activateConsumers(enabledConsumerHolders);
                LOGGER.info("Activated enabled consumers");
            } catch (IOException e) {
                LOGGER.error("Failed to activate enabled consumers", e);
                deactivateConsumers(enabledConsumerHolders);
            }catch(TimeoutException te){
                LOGGER.error("Failed to activate enabled consumers", te);
                deactivateConsumers(enabledConsumerHolders);
            }
        }


        public void onConnectionLost(Connection connection) {
            LOGGER.warn("Connection lost");
            LOGGER.info("Deactivating enabled consumers");
            List<ConsumerHolder> enabledConsumerHolders = filterConsumersForEnabledFlag(true);
            deactivateConsumers(enabledConsumerHolders);
        }


        public void onConnectionClosed(Connection connection) {
            LOGGER.warn("Connection closed for ever");
            LOGGER.info("Deactivating enabled consumers");
            List<ConsumerHolder> enabledConsumerHolders = filterConsumersForEnabledFlag(true);
            deactivateConsumers(enabledConsumerHolders);
        }
    }
    
    public class ConsumerHolder {

        Channel channel;
        Consumer consumer;
        ConsumerConfiguration configuration;
        ShutdownListener channelShutdownListener;

        boolean enabled = false;
        boolean active = false;
        
        public ConsumerHolder(Consumer consumer, ConsumerConfiguration configuration) {
            this.consumer = consumer;
            this.configuration = configuration;
            if ( consumer instanceof ManagedConsumer) {
                ((ManagedConsumer) consumer).setConfiguration(configuration);
            }
        }
        
        public Consumer getConsumer() {
            return consumer;
        }

        public ConsumerConfiguration getConfiguration() {
            return configuration;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isActive() {
            return active;
        }

        public void enable() throws IOException,TimeoutException {
            enabled = true;
            activate();
        }

        public void disable() {
            enabled = false;
            deactivate();
        }

        void deactivate() {
            LOGGER.info("Deactivating consumer of class {}", consumer.getClass());
            if (channel != null) {
                try {
                    LOGGER.info("Closing channel for consumer of class {}", consumer.getClass());
                    channel.close();
                    LOGGER.info("Closed channel for consumer of class {}", consumer.getClass());
                } catch (Exception e) {
                    LOGGER.info("Aborted closing channel for consumer of class {} (already closing)", consumer.getClass());
                }
                channel = null;
            }
            active = false;
            LOGGER.info("Deactivated consumer of class {}", consumer.getClass());
        }

        void activate() throws IOException,TimeoutException {
            LOGGER.info("Activating consumer of class {}", consumer.getClass());
            if (isActive()) {
                deactivate();
            }

            try {
                channel = createChannel();
                if (consumer instanceof ManagedConsumer) {
                    ((ManagedConsumer) consumer).setChannel(channel);
                }
                channel.basicConsume(configuration.getQueueName(), configuration.isAutoAck(), consumer);
                channel.basicQos(configuration.getPrefetchMessageCount());
                active = true;
                LOGGER.info("Activated consumer of class {}", consumer.getClass());
            } catch (IOException e) {
                LOGGER.error("Failed to activate consumer of class {}", consumer.getClass(), e);
                throw e;
            }
        }
    }

    public static abstract class ManagedConsumer implements Consumer {

        private Channel channel;
        private ConsumerConfiguration configuration;

        void setChannel(Channel channel) {
            this.channel = channel;
        }

        protected Channel getChannel() {
            return channel;
        }

        void setConfiguration(ConsumerConfiguration configuration) {
            this.configuration = configuration;
        }

        protected ConsumerConfiguration getConfiguration() {
            return configuration;
        }
    }

}