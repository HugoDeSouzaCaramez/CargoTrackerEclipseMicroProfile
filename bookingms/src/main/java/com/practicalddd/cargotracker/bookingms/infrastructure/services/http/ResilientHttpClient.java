package com.practicalddd.cargotracker.bookingms.infrastructure.services.http;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Logger;

@ApplicationScoped
public class ResilientHttpClient {
    
    private static final Logger logger = Logger.getLogger(ResilientHttpClient.class.getName());
    
    // Configurações com valores padrão
    private static final int MAX_RETRIES = 3;
    private static final Duration INITIAL_BACKOFF = Duration.ofMillis(100);
    private static final Duration MAX_BACKOFF = Duration.ofSeconds(5);
    
    /**
     * Executa uma operação com retry básico
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                attempt++;
                
                if (attempt >= MAX_RETRIES) {
                    break;
                }
                
                // Backoff exponencial
                long backoffMillis = calculateBackoff(attempt);
                logger.warning(String.format(
                    "Attempt %d/%d failed for %s. Retrying in %d ms: %s",
                    attempt, MAX_RETRIES, operationName, backoffMillis, e.getMessage()
                ));
                
                try {
                    Thread.sleep(backoffMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Operation interrupted", ie);
                }
            }
        }
        
        throw new RuntimeException(
            String.format("Failed to execute %s after %d attempts", operationName, MAX_RETRIES),
            lastException
        );
    }
    
    /**
     * Circuit breaker simples
     */
    public <T> T executeWithCircuitBreaker(Supplier<T> operation, String operationName, 
                                           Supplier<T> fallback) {
        // Implementação simplificada - em produção usar biblioteca como Resilience4j
        try {
            return operation.get();
        } catch (Exception e) {
            logger.warning(String.format(
                "Circuit breaker fallback for %s: %s", operationName, e.getMessage()
            ));
            
            if (fallback != null) {
                return fallback.get();
            }
            
            throw new RuntimeException("Operation failed and no fallback available", e);
        }
    }
    
    /**
     * Timeout básico
     */
    public <T> T executeWithTimeout(Supplier<T> operation, String operationName, 
                                    Duration timeout) {
        // Implementação simplificada - em produção usar CompletableFuture ou similar
        long startTime = System.currentTimeMillis();
        T result = operation.get();
        long duration = System.currentTimeMillis() - startTime;
        
        if (duration > timeout.toMillis()) {
            logger.warning(String.format(
                "Operation %s took %d ms (timeout: %d ms)",
                operationName, duration, timeout.toMillis()
            ));
        }
        
        return result;
    }
    
    private long calculateBackoff(int attempt) {
        long backoff = INITIAL_BACKOFF.toMillis() * (long) Math.pow(2, attempt - 1);
        return Math.min(backoff, MAX_BACKOFF.toMillis());
    }
}
