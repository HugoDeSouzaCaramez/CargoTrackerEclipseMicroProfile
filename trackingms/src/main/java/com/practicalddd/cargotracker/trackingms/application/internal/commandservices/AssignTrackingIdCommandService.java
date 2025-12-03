package com.practicalddd.cargotracker.trackingms.application.internal.commandservices;

import com.practicalddd.cargotracker.trackingms.application.ports.output.TrackingEventPublisher;
import com.practicalddd.cargotracker.trackingms.application.ports.output.TrackingRepository;
import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingActivity;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AddTrackingEventCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AssignTrackingNumberCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.BookingId;
import com.practicalddd.cargotracker.trackingms.domain.model.valueobjects.TrackingNumber;
import com.practicalddd.cargotracker.trackingms.domain.service.TrackingDomainService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
@Transactional
public class AssignTrackingIdCommandService {

    private static final Logger logger = Logger.getLogger(AssignTrackingIdCommandService.class.getName());

    @Inject
    private TrackingRepository trackingRepository;

    @Inject
    private TrackingEventPublisher eventPublisher;

    @Inject
    private TrackingDomainService trackingDomainService;

    @Transactional
    public TrackingNumber assignTrackingNumberToCargo(AssignTrackingNumberCommand command) {
        logger.info("=== INICIANDO assignTrackingNumberToCargo ===");
        logger.info("Booking ID: " + command.getBookingId());

        try {
            // Gerar número de tracking
            String trackingNumberStr = trackingRepository.generateNextTrackingNumber();
            logger.info("Tracking number gerado: " + trackingNumberStr);

            command.setTrackingNumber(trackingNumberStr);

            // Criar atividade usando o serviço de domínio
            logger.info("Criando TrackingActivity...");
            TrackingActivity activity = trackingDomainService.createTrackingActivity(command);
            logger.info("TrackingActivity criada");

            // Salvar no repositório
            logger.info("Salvando no repositório...");
            trackingRepository.save(activity);
            logger.info("Salvo no repositório");

            // Publicar eventos de domínio
            logger.info("Publicando eventos...");
            eventPublisher.publishDomainEvents(activity);

            logger.info("✅ Tracking number atribuído com sucesso: " + trackingNumberStr);
            return new TrackingNumber(trackingNumberStr);

        } catch (Exception e) {
            logger.severe("❌ ERRO em assignTrackingNumberToCargo: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void addTrackingEvent(AddTrackingEventCommand command) {
        logger.info("=== INICIANDO addTrackingEvent ===");
        logger.info("Booking ID: " + command.getBookingId());
        logger.info("Event Type: " + command.getEventType());

        try {
            // Validar evento
            trackingDomainService.validateTrackingEvent(command);

            // Buscar atividade existente
            logger.info("Buscando TrackingActivity existente...");
            Optional<TrackingActivity> optionalActivity = trackingRepository
                    .findByBookingId(new BookingId(command.getBookingId()));

            TrackingActivity trackingActivity;

            if (optionalActivity.isPresent()) {
                trackingActivity = optionalActivity.get();
                logger.info("TrackingActivity encontrada: " +
                        trackingActivity.getTrackingNumber().getTrackingNumber());
            } else {
                logger.warning("Nenhuma TrackingActivity encontrada para booking: " +
                        command.getBookingId() + ". Criando nova...");

                String trackingNumber = trackingRepository.generateNextTrackingNumber();
                logger.info("Novo tracking number: " + trackingNumber);

                AssignTrackingNumberCommand assignCommand = new AssignTrackingNumberCommand(
                        command.getBookingId(), trackingNumber);

                trackingActivity = trackingDomainService.createTrackingActivity(assignCommand);
                logger.info("Nova TrackingActivity criada");
            }

            // Adicionar evento usando serviço de domínio
            logger.info("Adicionando evento à atividade...");
            trackingDomainService.addTrackingEventToActivity(trackingActivity, command);

            // Salvar atividade atualizada
            logger.info("Salvando atividade atualizada...");
            trackingRepository.save(trackingActivity);

            // Publicar eventos de domínio
            logger.info("Publicando eventos...");
            eventPublisher.publishDomainEvents(trackingActivity);

            logger.info("✅ Evento de tracking armazenado com sucesso");

        } catch (Exception e) {
            logger.severe("❌ ERRO em addTrackingEvent: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

}
