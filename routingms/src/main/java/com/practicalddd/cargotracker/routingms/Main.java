package com.practicalddd.cargotracker.routingms;

import java.io.IOException;
import java.util.logging.LogManager;

import com.practicalddd.cargotracker.routingms.infrastructure.bootstrap.DataInitializer;
import io.helidon.microprofile.server.Server;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

public final class Main {

    private Main() { }

    public static void main(final String[] args) throws IOException {
        setupLogging();
        Server server = startServer();
        
        // Banco primeiro (RoutingMS não usa EventBinders)
        BeanManager beanManager = server.cdiContainer().getBeanManager();
        
        // 1. Primeiro inicializa o banco (cria tabelas e dados iniciais)
        initializeDatabase(beanManager);
        
        // RoutingMS não usa EventBinders (apenas expõe API REST)
        System.out.println("✅ RoutingMS started - Database initialized (No EventBinders needed)");
    }

    static Server startServer() {
        return Server.create().start();
    }

    private static void setupLogging() throws IOException {
        LogManager.getLogManager().readConfiguration(
                Main.class.getResourceAsStream("/logging.properties"));
    }

    private static void initializeDatabase(BeanManager beanManager) {
        try {
            System.out.println("🔄 Initializing RoutingMS Database...");
            
            // Força a criação das tabelas e dados iniciais
            Bean<DataInitializer> dataInitializerBean = 
                (Bean<DataInitializer>) beanManager.resolve(
                    beanManager.getBeans(DataInitializer.class));
            if (dataInitializerBean != null) {
                DataInitializer dataInitializer = beanManager.getContext(
                    dataInitializerBean.getScope()).get(dataInitializerBean, 
                    beanManager.createCreationalContext(dataInitializerBean));
                System.out.println("✅ RoutingMS Database initialized");
            } else {
                System.out.println("⚠️ No DataInitializer found for RoutingMS");
            }
        } catch (Exception e) {
            System.err.println("❌ Error initializing RoutingMS database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}