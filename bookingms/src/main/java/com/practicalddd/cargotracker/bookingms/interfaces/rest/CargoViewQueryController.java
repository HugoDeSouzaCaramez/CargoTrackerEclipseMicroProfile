package com.practicalddd.cargotracker.bookingms.interfaces.rest;

import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.CargoView;
import com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.repositories.CargoReadRepository;
import com.practicalddd.cargotracker.bookingms.application.services.CargoProjectionService;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.CargoViewResource;
import com.practicalddd.cargotracker.bookingms.interfaces.rest.dto.ErrorResponse;
import com.practicalddd.cargotracker.bookingms.infrastructure.persistence.mappers.CargoViewMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controlador REST para consultas otimizadas usando views materializadas.
 * Esta é uma implementação CQRS pura - apenas leituras.
 */
@Path("/cargoqueries/views")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class CargoViewQueryController {
    
    private static final Logger logger = Logger.getLogger(CargoViewQueryController.class.getName());
    
    @Inject
    private CargoReadRepository cargoReadRepository;
    
    @Inject
    private CargoProjectionService cargoProjectionService;
    
    @Inject
    private CargoViewMapper cargoViewMapper;
    
    @GET
    @Path("/all")
    public Response getAllCargoViews() {
        try {
            List<CargoView> views = cargoReadRepository.findAllViews();
            List<CargoViewResource> resources = cargoViewMapper.toResourceList(views);
            return Response.ok(resources).build();
        } catch (Exception e) {
            logger.severe("Error getting all cargo views: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to retrieve cargo views"))
                    .build();
        }
    }
    
    @GET
    @Path("/{bookingId}")
    public Response getCargoViewById(@PathParam("bookingId") String bookingId) {
        try {
            return cargoReadRepository.findViewByBookingId(bookingId)
                    .map(view -> {
                        CargoViewResource resource = cargoViewMapper.toResource(view);
                        return Response.ok(resource).build();
                    })
                    .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(ErrorResponse.notFound("Cargo view not found for ID: " + bookingId))
                            .build());
        } catch (Exception e) {
            logger.severe("Error getting cargo view by ID: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to retrieve cargo view"))
                    .build();
        }
    }
    
    @GET
    @Path("/status/{status}")
    public Response getCargoViewsByStatus(@PathParam("status") String status) {
        try {
            List<CargoView> views = cargoReadRepository.findViewsByStatus(status);
            List<CargoViewResource> resources = cargoViewMapper.toResourceList(views);
            return Response.ok(resources).build();
        } catch (Exception e) {
            logger.severe("Error getting cargo views by status: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to retrieve cargo views by status"))
                    .build();
        }
    }
    
    @GET
    @Path("/origin/{origin}")
    public Response getCargoViewsByOrigin(@PathParam("origin") String origin) {
        try {
            List<CargoView> views = cargoReadRepository.findViewsByOrigin(origin);
            List<CargoViewResource> resources = cargoViewMapper.toResourceList(views);
            return Response.ok(resources).build();
        } catch (Exception e) {
            logger.severe("Error getting cargo views by origin: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to retrieve cargo views by origin"))
                    .build();
        }
    }
    
    @GET
    @Path("/destination/{destination}")
    public Response getCargoViewsByDestination(@PathParam("destination") String destination) {
        try {
            List<CargoView> views = cargoReadRepository.findViewsByDestination(destination);
            List<CargoViewResource> resources = cargoViewMapper.toResourceList(views);
            return Response.ok(resources).build();
        } catch (Exception e) {
            logger.severe("Error getting cargo views by destination: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to retrieve cargo views by destination"))
                    .build();
        }
    }
    
    @GET
    @Path("/urgent")
    public Response getUrgentCargoViews() {
        try {
            // Cargas com deadline nos próximos 7 dias
            List<CargoView> urgentViews = cargoReadRepository.findViewsWithUpcomingDeadline(7);
            List<CargoViewResource> resources = cargoViewMapper.toResourceList(urgentViews);
            return Response.ok(resources).build();
        } catch (Exception e) {
            logger.severe("Error getting urgent cargo views: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to retrieve urgent cargo views"))
                    .build();
        }
    }
    
    @GET
    @Path("/delayed")
    public Response getDelayedCargoViews() {
        try {
            List<CargoView> allViews = cargoReadRepository.findAllViews();
            
            // Filtra apenas os cargos atrasados
            List<CargoView> delayedViews = allViews.stream()
                    .filter(view -> view.getArrivalDeadline() != null && 
                                   view.getArrivalDeadline().isBefore(LocalDateTime.now()) &&
                                   !"COMPLETED".equals(view.getStatus()) && 
                                   !"CLAIMED".equals(view.getStatus()) &&
                                   !"CANCELLED".equals(view.getStatus()))
                    .collect(Collectors.toList());
            
            List<CargoViewResource> resources = cargoViewMapper.toResourceList(delayedViews);
            return Response.ok(resources).build();
        } catch (Exception e) {
            logger.severe("Error getting delayed cargo views: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to retrieve delayed cargo views"))
                    .build();
        }
    }
    
    @GET
    @Path("/search")
    public Response searchCargoViews(
            @QueryParam("origin") String origin,
            @QueryParam("destination") String destination,
            @QueryParam("status") String status,
            @QueryParam("bookingId") String bookingId) {
        try {
            List<CargoView> results;
            
            if (bookingId != null && !bookingId.trim().isEmpty()) {
                // Busca específica por ID
                results = cargoReadRepository.findViewByBookingId(bookingId)
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
            } else if (origin != null && destination != null) {
                // Busca por origem e destino
                results = cargoReadRepository.findViewsByOriginAndDestination(origin, destination);
            } else if (origin != null) {
                // Busca por origem
                results = cargoReadRepository.findViewsByOrigin(origin);
            } else if (destination != null) {
                // Busca por destino
                results = cargoReadRepository.findViewsByDestination(destination);
            } else if (status != null) {
                // Busca por status
                results = cargoReadRepository.findViewsByStatus(status);
            } else {
                // Retorna todos se nenhum filtro for especificado
                results = cargoReadRepository.findAllViews();
            }
            
            List<CargoViewResource> resources = cargoViewMapper.toResourceList(results);
            return Response.ok(resources).build();
        } catch (Exception e) {
            logger.severe("Error searching cargo views: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to search cargo views"))
                    .build();
        }
    }
    
    @GET
    @Path("/paginated")
    public Response getPaginatedCargoViews(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        try {
            if (page < 0 || size <= 0 || size > 100) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.badRequest("Invalid pagination parameters"))
                        .build();
            }
            
            List<CargoView> views = cargoReadRepository.findViewsPaginated(page, size);
            List<CargoViewResource> resources = cargoViewMapper.toResourceList(views);
            long totalCount = cargoReadRepository.countAllViews();
            
            // Retorna com headers de paginação
            return Response.ok(resources)
                    .header("X-Total-Count", totalCount)
                    .header("X-Page", page)
                    .header("X-Page-Size", size)
                    .header("X-Total-Pages", (int) Math.ceil((double) totalCount / size))
                    .build();
        } catch (Exception e) {
            logger.severe("Error getting paginated cargo views: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to retrieve paginated cargo views"))
                    .build();
        }
    }
    
    @GET
    @Path("/stats")
    public Response getCargoViewStats() {
        try {
            long totalCount = cargoReadRepository.countAllViews();
            
            // Contagem por status
            Map<String, Long> statusCounts = new HashMap<>();
            List<CargoView> allViews = cargoReadRepository.findAllViews();
            
            for (CargoView view : allViews) {
                String status = view.getStatus();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
            }
            
            // Contagem por origem (top 10)
            Map<String, Long> originCounts = new HashMap<>();
            for (CargoView view : allViews) {
                String origin = view.getOriginLocation();
                originCounts.put(origin, originCounts.getOrDefault(origin, 0L) + 1);
            }
            
            // Contagem por destino (top 10)
            Map<String, Long> destinationCounts = new HashMap<>();
            for (CargoView view : allViews) {
                String destination = view.getDestinationLocation();
                destinationCounts.put(destination, destinationCounts.getOrDefault(destination, 0L) + 1);
            }
            
            // Carregos urgentes
            long urgentCount = allViews.stream()
                    .filter(CargoView::isUrgent)
                    .count();
            
            // Carregos atrasados
            long delayedCount = allViews.stream()
                    .filter(view -> view.getArrivalDeadline() != null && 
                                   view.getArrivalDeadline().isBefore(LocalDateTime.now()) &&
                                   !"COMPLETED".equals(view.getStatus()) && 
                                   !"CLAIMED".equals(view.getStatus()) &&
                                   !"CANCELLED".equals(view.getStatus()))
                    .count();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCargos", totalCount);
            stats.put("urgentCargos", urgentCount);
            stats.put("delayedCargos", delayedCount);
            stats.put("statusDistribution", statusCounts);
            stats.put("topOrigins", limitMap(originCounts, 10));
            stats.put("topDestinations", limitMap(destinationCounts, 10));
            
            return Response.ok(stats).build();
        } catch (Exception e) {
            logger.severe("Error getting cargo view stats: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to retrieve cargo view statistics"))
                    .build();
        }
    }
    
    @GET
    @Path("/health")
    public Response getProjectionHealth() {
        try {
            long viewCount = cargoReadRepository.countAllViews();
            
            Map<String, Object> healthInfo = new HashMap<>();
            healthInfo.put("status", "HEALTHY");
            healthInfo.put("timestamp", LocalDateTime.now().toString());
            healthInfo.put("viewCount", viewCount);
            healthInfo.put("projectionService", "ACTIVE");
            
            // Verifica se há descompasso entre agregados e views
            // (No futuro, sofisticar mais)
            healthInfo.put("notes", "Projection system is operational");
            
            return Response.ok(healthInfo).build();
        } catch (Exception e) {
            Map<String, Object> healthInfo = new HashMap<>();
            healthInfo.put("status", "UNHEALTHY");
            healthInfo.put("timestamp", LocalDateTime.now().toString());
            healthInfo.put("error", e.getMessage());
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(healthInfo)
                    .build();
        }
    }
    
    @POST
    @Path("/rebuild")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response rebuildProjections() {
        try {
            logger.info("Manual projection rebuild requested");
            
            // No futuro, fazer assincronamente em um job
            // Mas para simplicidade, fazemos síncrono aqui
            cargoProjectionService.rebuildAllProjections();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Projection rebuild initiated successfully");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("status", "IN_PROGRESS");
            
            return Response.accepted()
                    .entity(response)
                    .build();
        } catch (Exception e) {
            logger.severe("Error rebuilding projections: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to rebuild projections: " + e.getMessage()))
                    .build();
        }
    }
    
    @DELETE
    @Path("/{bookingId}")
    public Response deleteCargoView(@PathParam("bookingId") String bookingId) {
        try {
            cargoReadRepository.findViewByBookingId(bookingId)
                    .ifPresent(view -> cargoReadRepository.deleteView(
                            new com.practicalddd.cargotracker.bookingms.domain.cargoaggregate.valueobjects.BookingId(bookingId)
                    ));
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cargo view deleted successfully");
            response.put("bookingId", bookingId);
            
            return Response.ok()
                    .entity(response)
                    .build();
        } catch (Exception e) {
            logger.severe("Error deleting cargo view: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalError("Failed to delete cargo view"))
                    .build();
        }
    }
    
    /**
     * Método auxiliar para limitar um mapa aos N primeiros itens
     */
    private Map<String, Long> limitMap(Map<String, Long> map, int limit) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        HashMap::new
                ));
    }
}
