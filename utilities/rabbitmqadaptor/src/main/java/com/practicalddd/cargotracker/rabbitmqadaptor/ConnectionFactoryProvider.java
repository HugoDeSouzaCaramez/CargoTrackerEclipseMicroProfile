package com.practicalddd.cargotracker.rabbitmqadaptor;

import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@ApplicationScoped
public class ConnectionFactoryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionFactoryProvider.class);

    @Produces
    @Singleton
    public ConnectionFactory provideConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        
        String rabbitmqHost = System.getenv().getOrDefault("RABBITMQ_HOST", "localhost");
        String rabbitmqPort = System.getenv().getOrDefault("RABBITMQ_PORT", "5672");
        String rabbitmqUsername = System.getenv().getOrDefault("RABBITMQ_USERNAME", "guest");
        String rabbitmqPassword = System.getenv().getOrDefault("RABBITMQ_PASSWORD", "guest");
        String rabbitmqVirtualHost = System.getenv().getOrDefault("RABBITMQ_VIRTUAL_HOST", "/");
        
        factory.setHost(rabbitmqHost);
        factory.setPort(Integer.parseInt(rabbitmqPort));
        factory.setUsername(rabbitmqUsername);
        factory.setPassword(rabbitmqPassword);
        factory.setVirtualHost(rabbitmqVirtualHost);
        factory.setConnectionTimeout(30000);
        factory.setRequestedHeartbeat(60);
        
        // ✅ Configurações de resiliência
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(5000);
        factory.setTopologyRecoveryEnabled(true);
        
        // ✅ Configurações de performance
        factory.setRequestedChannelMax(100);
        factory.setRequestedFrameMax(1024 * 1024); // 1MB
        
        LOGGER.info("✅ ConnectionFactory configurada para: {}:{} (vhost: {})", 
                   rabbitmqHost, rabbitmqPort, rabbitmqVirtualHost);
        
        return factory;
    }
}