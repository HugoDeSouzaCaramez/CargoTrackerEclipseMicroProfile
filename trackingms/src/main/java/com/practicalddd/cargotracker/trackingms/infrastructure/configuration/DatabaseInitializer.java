package com.practicalddd.cargotracker.trackingms.infrastructure.configuration;

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
            logger.info("🎯 === INICIALIZANDO BANCO DE DADOS TRACKINGMS ===");
            
            // Forçar criação das tabelas executando uma consulta que falha se não existir
            // e capturando a exceção
            try {
                entityManager.createNativeQuery("SELECT 1 FROM tracking_activity LIMIT 1").getResultList();
                logger.info("✅ Tabelas já existem");
            } catch (Exception e) {
                logger.info("ℹ️ Tabelas serão criadas pelo JPA...");
                
                // Criar uma entidade de teste para forçar criação
                entityManager.createNativeQuery(
                    "CREATE TABLE IF NOT EXISTS tracking_activity_test (id INT)").executeUpdate();
                entityManager.createNativeQuery("DROP TABLE tracking_activity_test").executeUpdate();
            }
            
            // Verificar conexão
            Object result = entityManager.createNativeQuery("SELECT 1").getSingleResult();
            logger.info("✅ Conexão com banco OK: " + result);
            
            logger.info("✅ === BANCO DE DADOS INICIALIZADO COM SUCESSO ===");
            
        } catch (Exception e) {
            logger.severe("❌ ERRO NA INICIALIZAÇÃO DO BANCO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
