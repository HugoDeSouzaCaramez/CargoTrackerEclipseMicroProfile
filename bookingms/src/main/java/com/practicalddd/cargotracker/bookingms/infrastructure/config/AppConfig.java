package com.practicalddd.cargotracker.bookingms.infrastructure.config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Optional;

@ApplicationScoped
public class AppConfig {
    
    @Inject
    @ConfigProperty(name = "routing.service.url", 
                   defaultValue = "http://routingms:8081/cargoRouting")
    private String routingServiceUrl;
    
    @Inject
    @ConfigProperty(name = "routing.service.timeout.seconds", defaultValue = "30")
    private int routingServiceTimeoutSeconds;
    
    @Inject
    @ConfigProperty(name = "http.retry.max.attempts", defaultValue = "3")
    private int maxRetryAttempts;
    
    @Inject
    @ConfigProperty(name = "http.retry.backoff.ms", defaultValue = "1000")
    private long retryBackoffMillis;
    
    @Inject
    @ConfigProperty(name = "supported.ports", defaultValue = "USNYC,NLRTM,GBLON,JPTYO,SGSIN,DEHAM,CNHKG,USLGB,CNPVG,HKHKG")
    private String supportedPorts;
    
    @Inject
    @ConfigProperty(name = "booking.max.amount", defaultValue = "1000000")
    private int maxBookingAmount;
    
    @Inject
    @ConfigProperty(name = "booking.min.deadline.hours", defaultValue = "24")
    private int minDeadlineHours;
    
    // Getters
    public String getRoutingServiceUrl() {
        return routingServiceUrl;
    }
    
    public int getRoutingServiceTimeoutSeconds() {
        return routingServiceTimeoutSeconds;
    }
    
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
    
    public long getRetryBackoffMillis() {
        return retryBackoffMillis;
    }
    
    public String[] getSupportedPorts() {
        return supportedPorts.split(",");
    }
    
    public int getMaxBookingAmount() {
        return maxBookingAmount;
    }
    
    public int getMinDeadlineHours() {
        return minDeadlineHours;
    }
}
