package com.practicalddd.cargotracker.routingms.interfaces.rest;

import com.practicalddd.cargotracker.routingms.application.internal.queryservices.CargoRoutingQueryService;
import com.practicalddd.cargotracker.routingms.domain.model.aggregates.Voyage;
import com.practicalddd.cargotracker.shareddomain.model.TransitPath;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Path("/cargoRouting")
@ApplicationScoped
public class CargoRoutingController {

    private static final Logger logger = Logger.getLogger(CargoRoutingController.class.getName());
    
    // Formatos de data suportados
    private static final DateTimeFormatter[] SUPPORTED_FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,  // "2026-01-15T23:59:59"
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"), // com nanossegundos
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),  // formato SQL
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy"), // formato original do log
        DateTimeFormatter.ofPattern("yyyy-MM-dd")  // apenas data
    };

    @Inject
    private CargoRoutingQueryService cargoRoutingQueryService;

    @GET
    @Path("/optimalRoute")
    @Produces(MediaType.APPLICATION_JSON)
    public TransitPath findOptimalRoute(
            @QueryParam("origin") String originUnLocode,
            @QueryParam("destination") String destinationUnLocode,
            @QueryParam("deadline") String deadline) {

        // Validação de parâmetros
        if (originUnLocode == null || originUnLocode.trim().isEmpty()) {
            throw new BadRequestException("Origin parameter is required");
        }
        if (destinationUnLocode == null || destinationUnLocode.trim().isEmpty()) {
            throw new BadRequestException("Destination parameter is required");
        }
        
        logger.info("Searching route from " + originUnLocode + " to " + destinationUnLocode);
        
        LocalDateTime deadlineDate = parseDeadline(deadline);
        return cargoRoutingQueryService.findOptimalRoute(originUnLocode, destinationUnLocode, deadlineDate);
    }

    @GET
    @Path("/voyages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Voyage> getAllVoyages() {
        return cargoRoutingQueryService.findAllVoyages();
    }

    private LocalDateTime parseDeadline(String deadline) {
        // Se deadline não for fornecido, usa 30 dias a partir de agora
        if (deadline == null || deadline.trim().isEmpty()) {
            return LocalDateTime.now().plusDays(30);
        }
        
        String normalizedDeadline = deadline.trim();
        
        // Tenta parsear com cada formatador suportado
        for (DateTimeFormatter formatter : SUPPORTED_FORMATTERS) {
            try {
                // Para formatos que incluem timezone, removemos o fuso horário
                if (formatter.toString().contains("z") || formatter.toString().contains("Z")) {
                    // Converte para ZonedDateTime e depois para LocalDateTime
                    return java.time.ZonedDateTime.parse(normalizedDeadline, formatter)
                            .toLocalDateTime();
                } else {
                    // Formato sem timezone
                    return LocalDateTime.parse(normalizedDeadline, formatter);
                }
            } catch (DateTimeParseException e) {
                // Continua tentando próximo formato
                continue;
            }
        }
        
        // Se nenhum formato funcionar, tenta parsear como Date (compatibilidade)
        try {
            // Usando SimpleDateFormat como fallback (para formatos legados)
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", java.util.Locale.ENGLISH);
            Date date = sdf.parse(normalizedDeadline);
            // Converte Date para LocalDateTime
            return date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (java.text.ParseException e1) {
            try {
                // Tenta outro formato comum
                java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                Date date2 = sdf2.parse(normalizedDeadline);
                return date2.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .withHour(23).withMinute(59).withSecond(59); // Fim do dia
            } catch (java.text.ParseException e2) {
                logger.warning("Failed to parse deadline: " + deadline + 
                             ". Using default (30 days from now).");
                // Fallback: 30 dias a partir de agora
                return LocalDateTime.now().plusDays(30);
            }
        }
    }
}
