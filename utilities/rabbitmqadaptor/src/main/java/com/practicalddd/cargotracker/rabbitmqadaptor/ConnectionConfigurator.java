package com.practicalddd.cargotracker.rabbitmqadaptor;


import com.rabbitmq.client.ConnectionFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ConnectionConfigurator {

    @Inject
    ConnectionFactory connectionFactory;

    public void configureFactory(Class<?> clazz) {
        ConnectionConfiguration connectionConfiguration = resolveConnectionConfiguration(clazz);
        if (connectionConfiguration == null) {
            return;
        }
        connectionFactory.setHost(connectionConfiguration.host());
        connectionFactory.setVirtualHost(connectionConfiguration.virtualHost());
        connectionFactory.setPort(connectionConfiguration.port());
        connectionFactory.setConnectionTimeout(connectionConfiguration.timeout());
        connectionFactory.setRequestedHeartbeat(connectionConfiguration.heartbeat());
        connectionFactory.setUsername(connectionConfiguration.username());
        connectionFactory.setPassword(connectionConfiguration.password());
        connectionFactory.setRequestedFrameMax(connectionConfiguration.frameMax());
    }

    ConnectionConfiguration resolveConnectionConfiguration(Class<?> clazz) {
        ConnectionConfiguration connectionConfiguration = clazz.getAnnotation(ConnectionConfiguration.class);
        if (connectionConfiguration != null) {
            return connectionConfiguration;
        }

        ConnectionConfigurations connectionConfigurations = clazz.getAnnotation(ConnectionConfigurations.class);
        if (connectionConfigurations != null) {
            String profileProperty = System.getProperty(ConnectionConfiguration.PROFILE_PROPERTY);
            String profile = profileProperty == null ? ConnectionConfiguration.DEFAULT_PROFILE : profileProperty;
            for (ConnectionConfiguration connectionConfigurationCandidate : connectionConfigurations.value()) {
                if (connectionConfigurationCandidate.profile().equalsIgnoreCase(profile)) {
                    return connectionConfigurationCandidate;
                }
            }
        }
        return null;
    }
}
