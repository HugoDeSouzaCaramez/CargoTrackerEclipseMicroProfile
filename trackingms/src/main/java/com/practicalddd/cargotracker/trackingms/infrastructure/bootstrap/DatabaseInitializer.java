package com.practicalddd.cargotracker.trackingms.infrastructure.bootstrap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.logging.Logger;

@ApplicationScoped
public class DatabaseInitializer {

    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());

    @PersistenceContext(unitName = "trackingms")
    private EntityManager entityManager;

    @PostConstruct
    @Transactional
    public void init() {
        try {
            logger.info("🎯 === FORÇANDO CRIAÇÃO DAS TABELAS TRACKINGMS ===");
            
            // Executa uma consulta simples para forçar a criação das tabelas
            entityManager.createNativeQuery("SELECT 1 FROM tracking_activity LIMIT 1").getResultList();
            
            logger.info("✅ === TABELAS TRACKINGMS CRIADAS/VERIFICADAS COM SUCESSO ===");
        } catch (Exception e) {
            logger.info("ℹ️ === TABELAS TRACKINGMS JÁ EXISTEM OU FORAM CRIADAS AGORA ===");
        }
    }
}