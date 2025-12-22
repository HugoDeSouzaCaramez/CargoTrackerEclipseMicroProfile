package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Controller para setup inicial das views CQRS
 */
@Path("/setup/cargoviews")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class CargoViewSetupController {
    
    private static final Logger logger = Logger.getLogger(CargoViewSetupController.class.getName());
    
    @PersistenceContext(unitName = "bookingms")
    private EntityManager entityManager;
    
    @GET
    @Path("/check")
    public Response checkTableExists() {
        try {
            boolean exists = checkCargoViewsTableExists();
            
            Map<String, Object> response = new HashMap<>();
            response.put("tableExists", exists);
            response.put("message", exists ? 
                "CargoViews table exists and is ready for CQRS" : 
                "CargoViews table does not exist");
            
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.severe("Error checking CargoViews table: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error checking table: " + e.getMessage())
                    .build();
        }
    }
    
    @GET
    @Path("/create")
    @Transactional
    public Response createTableIfNotExists() {
        try {
            if (checkCargoViewsTableExists()) {
                return Response.ok()
                        .entity("CargoViews table already exists")
                        .build();
            }
            
            // Tentar criar a tabela usando Native Query
            String createTableSQL = "CREATE TABLE cargo_views (" +
                "booking_id VARCHAR(50) PRIMARY KEY, " +
                "booking_amount INTEGER NOT NULL, " +
                "origin_location VARCHAR(10) NOT NULL, " +
                "destination_location VARCHAR(10) NOT NULL, " +
                "arrival_deadline TIMESTAMP NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "routing_status VARCHAR(50), " +
                "transport_status VARCHAR(50), " +
                "last_known_location VARCHAR(10), " +
                "current_voyage VARCHAR(50), " +
                "leg_count INTEGER, " +
                "estimated_transit_hours BIGINT, " +
                "is_on_track BOOLEAN, " +
                "is_misdirected BOOLEAN, " +
                "is_ready_for_claim BOOLEAN, " +
                "created_at TIMESTAMP NOT NULL, " +
                "last_updated TIMESTAMP NOT NULL, " +
                "version BIGINT, " +
                "aggregate_version BIGINT" +
                ")";
            
            entityManager.createNativeQuery(createTableSQL).executeUpdate();
            
            // Criar índices
            String[] indexSQLs = {
                "CREATE INDEX idx_cargo_view_status ON cargo_views(status)",
                "CREATE INDEX idx_cargo_view_origin ON cargo_views(origin_location)",
                "CREATE INDEX idx_cargo_view_dest ON cargo_views(destination_location)",
                "CREATE INDEX idx_cargo_view_deadline ON cargo_views(arrival_deadline)",
                "CREATE INDEX idx_cargo_view_last_updated ON cargo_views(last_updated)",
                "CREATE INDEX idx_cargo_view_origin_dest ON cargo_views(origin_location, destination_location)",
                "CREATE INDEX idx_cargo_view_routing_status ON cargo_views(routing_status)"
            };
            
            for (String indexSQL : indexSQLs) {
                try {
                    entityManager.createNativeQuery(indexSQL).executeUpdate();
                } catch (Exception e) {
                    logger.warning("Error creating index (may already exist): " + e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CargoViews table created successfully");
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.severe("Error creating CargoViews table: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating table: " + e.getMessage())
                    .build();
        }
    }
    
    private boolean checkCargoViewsTableExists() {
        try {
            // Verificar se a tabela existe usando uma consulta
            entityManager.createNativeQuery(
                "SELECT 1 FROM cargo_views LIMIT 1")
                .getSingleResult();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
