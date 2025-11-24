package com.practicalddd.cargotracker.rabbitmqadaptor;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

public class CDIExtension implements Extension {
    
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        System.out.println("RabbitMQ Adaptor CDI Extension loaded");
    }
}