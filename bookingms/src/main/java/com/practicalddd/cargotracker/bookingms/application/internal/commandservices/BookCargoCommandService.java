package com.practicalddd.cargotracker.bookingms.application.internal.commandservices;

import com.practicalddd.cargotracker.bookingms.application.internal.events.DomainEventPublisher;
import com.practicalddd.cargotracker.bookingms.application.ports.inbound.CargoBookingCommandPort;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.AuditService;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.BillingService;
import com.practicalddd.cargotracker.bookingms.application.ports.outbound.NotificationService;
import com.practicalddd.cargotracker.bookingms.domain.model.aggregates.Cargo;
import com.practicalddd.cargotracker.bookingms.domain.model.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.model.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.factory.CargoFactory;
import com.practicalddd.cargotracker.bookingms.domain.model.repositories.CargoRepository;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.bookingms.domain.model.valueobjects.Location;
import com.practicalddd.cargotracker.bookingms.infrastructure.config.AppConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Serviço de aplicação para processar comandos de booking de cargas.
 * Implementa apenas operações de escrita (commands).
 */
@ApplicationScoped
public class BookCargoCommandService implements CargoBookingCommandPort {

    @Inject
    private CargoRepository cargoRepository;

    @Inject
    private DomainEventPublisher eventPublisher;

    @Inject
    private TransactionalService transactionalService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private BillingService billingService;

    @Inject
    private AuditService auditService;

    @Inject
    private AppConfig appConfig;

    // Lista de portos suportados pelo sistema - agora carregada da configuração
    private Set<String> getSupportedPorts() {
        return new HashSet<>(Arrays.asList(appConfig.getSupportedPorts()));
    }

    @Override
    @Transactional
    public BookingId bookCargo(BookCargoCommand bookCargoCommand) {
        // Validação adicional do comando - REGRAS DE APLICAÇÃO
        if (bookCargoCommand.getDestArrivalDeadline()
                .isBefore(LocalDateTime.now().plusHours(appConfig.getMinDeadlineHours()))) {
            throw new IllegalArgumentException(
                String.format("Arrival deadline must be at least %d hours from now", 
                             appConfig.getMinDeadlineHours())
            );
        }

        // Validação adicional: verificar se portos são suportados pelo sistema
        validateSupportedPorts(bookCargoCommand.getOriginLocation(), bookCargoCommand.getDestLocation());

        // Validação adicional: verificar conflitos de booking
        validateNoDuplicateBooking(bookCargoCommand);

        String bookingId = cargoRepository.nextBookingId();

        // Cria e armazena o cargo
        Cargo cargo = CargoFactory.createCargo(bookCargoCommand, bookingId);
        cargoRepository.store(cargo);

        // Publica evento de domínio
        eventPublisher.publish(new CargoBookedEvent(bookingId));
        
        // 1. Calcular tarifa
        try {
            boolean isUrgent = isUrgentBooking(bookCargoCommand.getDestArrivalDeadline());
            BigDecimal fee = billingService.calculateFee(
                new Location(bookCargoCommand.getOriginLocation()),
                new Location(bookCargoCommand.getDestLocation()),
                bookCargoCommand.getBookingAmount(),
                bookCargoCommand.getDestArrivalDeadline(),
                isUrgent
            );
            
            System.out.println(String.format(
                "[BILLING] Fee calculated for booking %s: $%s (urgent: %s)",
                bookingId, fee, isUrgent
            ));
            
            // Validação de crédito (simulando um ID de cliente)
            String customerId = extractCustomerId(bookCargoCommand);
            if (billingService.validateCredit(customerId, fee)) {
                String invoiceNumber = billingService.generateInvoice(bookingId, fee, customerId);
                System.out.println(String.format(
                    "[BILLING] Invoice generated: %s", invoiceNumber
                ));
            }
        } catch (Exception e) {
            System.err.println("[BILLING] Error processing billing: " + e.getMessage());
            // Não propaga a exceção para não afetar o booking principal
        }

        // 2. Registrar auditoria
        try {
            auditService.logAction(
                "CREATE_BOOKING",
                "Cargo",
                bookingId,
                "system", // No futuro, obter do contexto de autenticação
                String.format("Booking from %s to %s, amount: %d, deadline: %s", 
                    bookCargoCommand.getOriginLocation(), 
                    bookCargoCommand.getDestLocation(),
                    bookCargoCommand.getBookingAmount(),
                    bookCargoCommand.getDestArrivalDeadline())
            );
            
            auditService.logDataChange(
                "Cargo",
                bookingId,
                "CREATED",
                "N/A",
                String.format("Origin: %s, Destination: %s", 
                    bookCargoCommand.getOriginLocation(), 
                    bookCargoCommand.getDestLocation())
            );
        } catch (Exception e) {
            System.err.println("[AUDIT] Error logging audit: " + e.getMessage());
            // Não propaga a exceção
        }

        // 3. Enviar notificações
        try {
            String customerEmail = extractCustomerEmail(bookCargoCommand);
            
            notificationService.notifyBookingCreated(
                bookingId,
                bookCargoCommand.getOriginLocation(),
                bookCargoCommand.getDestLocation(),
                customerEmail
            );
            
            System.out.println(String.format(
                "[NOTIFICATION] Sent booking confirmation for %s to %s",
                bookingId, customerEmail
            ));
        } catch (Exception e) {
            System.err.println("[NOTIFICATION] Error sending notification: " + e.getMessage());
            // Não propaga a exceção
        }

        return new BookingId(bookingId);
    }

    /**
     * Método alternativo que usa controle transacional explícito (programático)
     * em vez de @Transactional declarativo.
     */
    public BookingId bookCargoWithExplicitTransaction(BookCargoCommand bookCargoCommand) {
        try {
            return transactionalService.executeInTransaction(() -> {
                // Mesmas validações
                if (bookCargoCommand.getDestArrivalDeadline()
                        .isBefore(LocalDateTime.now().plusHours(appConfig.getMinDeadlineHours()))) {
                    throw new IllegalArgumentException(
                        String.format("Arrival deadline must be at least %d hours from now", 
                                     appConfig.getMinDeadlineHours())
                    );
                }

                validateSupportedPorts(bookCargoCommand.getOriginLocation(), bookCargoCommand.getDestLocation());
                validateNoDuplicateBooking(bookCargoCommand);

                String bookingId = cargoRepository.nextBookingId();
                Cargo cargo = CargoFactory.createCargo(bookCargoCommand, bookingId);
                cargoRepository.store(cargo);

                eventPublisher.publish(new CargoBookedEvent(bookingId));

                
                // Calcular tarifa
                boolean isUrgent = isUrgentBooking(bookCargoCommand.getDestArrivalDeadline());
                BigDecimal fee = billingService.calculateFee(
                    new Location(bookCargoCommand.getOriginLocation()),
                    new Location(bookCargoCommand.getDestLocation()),
                    bookCargoCommand.getBookingAmount(),
                    bookCargoCommand.getDestArrivalDeadline(),
                    isUrgent
                );
                
                System.out.println(String.format(
                    "[BILLING] Fee calculated (explicit transaction): $%s", fee
                ));

                // Registrar auditoria
                auditService.logAction(
                    "CREATE_BOOKING_EXPLICIT_TX",
                    "Cargo",
                    bookingId,
                    "system",
                    "Booking created with explicit transaction"
                );

                // Notificação
                String customerEmail = extractCustomerEmail(bookCargoCommand);
                notificationService.notifyBookingCreated(
                    bookingId,
                    bookCargoCommand.getOriginLocation(),
                    bookCargoCommand.getDestLocation(),
                    customerEmail
                );

                return new BookingId(bookingId);
            });
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to book cargo with explicit transaction", e);
        }
    }

    private void validateSupportedPorts(String origin, String destination) {
        String originUpper = origin.toUpperCase();
        String destUpper = destination.toUpperCase();
        Set<String> supportedPorts = getSupportedPorts();
        
        if (!supportedPorts.contains(originUpper)) {
            throw new IllegalArgumentException(
                String.format("Origin port '%s' is not supported. Supported ports: %s", 
                    origin, String.join(", ", supportedPorts))
            );
        }
        
        if (!supportedPorts.contains(destUpper)) {
            throw new IllegalArgumentException(
                String.format("Destination port '%s' is not supported. Supported ports: %s", 
                    destination, String.join(", ", supportedPorts))
            );
        }
    }

    private void validateNoDuplicateBooking(BookCargoCommand command) {
        System.out.println("Validating no duplicate booking for: " + 
            command.getOriginLocation() + " -> " + command.getDestLocation());
    }

    /**
     * Verifica se o booking é urgente (prazo menor que 7 dias)
     */
    private boolean isUrgentBooking(LocalDateTime deadline) {
        long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDateTime.now(), deadline);
        return daysUntilDeadline < 7;
    }

    /**
     * Extrai email do cliente do comando (simulação)
     * No futuro, o comando terá esta informação
     */
    private String extractCustomerEmail(BookCargoCommand command) {
        // Simulação: no futuro, isso virá do comando ou de um serviço de clientes
        return "customer-" + command.getOriginLocation().toLowerCase() + "@example.com";
    }

    /**
     * Extrai ID do cliente (simulação)
     */
    private String extractCustomerId(BookCargoCommand command) {
        // Simulação: gerar um ID baseado na origem
        return "CUST-" + command.getOriginLocation().toUpperCase().hashCode();
    }
}
