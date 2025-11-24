package com.practicalddd.cargotracker.rabbitmqadaptor;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class RabbitMQHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQHealthCheck.class);

    @Inject
    ConnectionFactory connectionFactory;

    /**
     * Verifica se o RabbitMQ está saudável
     */
    public boolean isHealthy() {
        try (Connection connection = connectionFactory.newConnection()) {
            boolean isOpen = connection.isOpen();
            if (isOpen) {
                LOGGER.debug("✅ RabbitMQ health check passed");
            }
            return isOpen;
        } catch (IOException | TimeoutException e) {
            LOGGER.warn("❌ RabbitMQ health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Aguarda até que o RabbitMQ fique saudável
     */
    public void waitUntilHealthy(int maxRetries, long retryIntervalMs) {
        int attempts = 0;
        while (attempts < maxRetries) {
            if (isHealthy()) {
                LOGGER.info("✅ RabbitMQ is healthy after {} attempts", attempts);
                return;
            }

            attempts++;
            LOGGER.warn("⚠️ RabbitMQ not healthy, attempt {}/{}", attempts, maxRetries);

            if (attempts < maxRetries) {
                try {
                    Thread.sleep(retryIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for RabbitMQ", e);
                }
            }
        }

        throw new RuntimeException("RabbitMQ not healthy after " + maxRetries + " attempts");
    }

    /**
     * Verificação de saúde com timeout customizado
     */
    public boolean isHealthyWithTimeout(int timeoutMs) {
        try {
            ConnectionFactory factory = connectionFactory;
            factory.setConnectionTimeout(timeoutMs);
            try (Connection connection = factory.newConnection()) {
                return connection.isOpen();
            }
        } catch (IOException | TimeoutException e) {
            LOGGER.debug("RabbitMQ health check timeout: {}", e.getMessage());
            return false;
        }
    }

    public boolean waitUntilReady(int maxWaitTimeSeconds) {
        int totalWaitTime = 0;
        int checkInterval = 2000; // 2 segundos

        while (totalWaitTime < maxWaitTimeSeconds * 1000) {
            if (isHealthy()) {
                LOGGER.info("✅ RabbitMQ está pronto após {} segundos", totalWaitTime / 1000);
                return true;
            }

            try {
                Thread.sleep(checkInterval);
                totalWaitTime += checkInterval;
                LOGGER.info("⏳ Aguardando RabbitMQ... ({}/{} segundos)",
                        totalWaitTime / 1000, maxWaitTimeSeconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        LOGGER.warn("⚠️ RabbitMQ não ficou pronto em {} segundos", maxWaitTimeSeconds);
        return false;
    }
}