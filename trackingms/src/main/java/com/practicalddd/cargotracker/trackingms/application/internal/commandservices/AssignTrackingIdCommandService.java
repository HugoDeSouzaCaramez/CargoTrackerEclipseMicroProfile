package com.practicalddd.cargotracker.trackingms.application.internal.commandservices;

import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingActivity;
import com.practicalddd.cargotracker.trackingms.domain.model.aggregates.TrackingNumber;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AddTrackingEventCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.commands.AssignTrackingNumberCommand;
import com.practicalddd.cargotracker.trackingms.domain.model.entities.BookingId;
import com.practicalddd.cargotracker.trackingms.infrastructure.repositories.jpa.TrackingRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.logging.Logger;

@ApplicationScoped
public class AssignTrackingIdCommandService {

    @Inject
    private TrackingRepository trackingRepository;

    private static final Logger logger = Logger.getLogger(AssignTrackingIdCommandService.class.getName());

    @Transactional
    public TrackingNumber assignTrackingNumberToCargo(AssignTrackingNumberCommand assignTrackingNumberCommand) {
        System.out.println(
                "===========================================================================================================");
        System.out.println(
                "===========================================================================================================");
        System.out.println(
                "===========================================================================================================");
        System.out.println(
                "===========================================================================================================");
        System.out.println(
                "===========================================================================================================");
        System.out.println(
                "===========================================================================================================");
        System.out.println(
                " public TrackingNumber assignTrackingNumberToCargo(AssignTrackingNumberCommand assignTrackingNumberCommand)");
        String trackingNumber = trackingRepository.nextTrackingNumber();
        assignTrackingNumberCommand.setTrackingNumber(trackingNumber);
        TrackingActivity activity = new TrackingActivity(assignTrackingNumberCommand);
        System.out.println("***Going to store in repository");
        trackingRepository.store(activity);
        System.out.println("?????????????????????????????????????????");
        System.out.println("?????????????????????????????????????????");
        System.out.println("?????????????????????????????????????????");
        System.out.println("?????????????????????????????????????????");
        System.out.println("?????????????????????????????????????????");
        System.out.println("?????????????????????????????????????????");
        System.out.println("?????????????????????????????????????????");
        System.out.println("?????????????????????????????????????????");
        return new TrackingNumber(trackingNumber);
    }

    @Transactional
    public void addTrackingEvent(AddTrackingEventCommand addTrackingEventCommand) {
        try {
            logger.info("Processing tracking event for booking: " +
                    addTrackingEventCommand.getBookingId());

            TrackingActivity trackingActivity = trackingRepository.find(
                    new BookingId(addTrackingEventCommand.getBookingId()));

            if (trackingActivity == null) {
                logger.warning("No TrackingActivity found for booking: " +
                        addTrackingEventCommand.getBookingId() + ". Creating new one automatically.");

                String trackingNumber = trackingRepository.nextTrackingNumber();
                AssignTrackingNumberCommand assignCommand = new AssignTrackingNumberCommand(
                        addTrackingEventCommand.getBookingId(), trackingNumber);
                trackingActivity = new TrackingActivity(assignCommand);

                logger.info("Created new TrackingActivity with tracking number: " + trackingNumber);
            }

            trackingActivity.addTrackingEvent(addTrackingEventCommand);
            trackingRepository.store(trackingActivity);

            logger.info("Successfully stored tracking event for booking: " +
                    addTrackingEventCommand.getBookingId() +
                    ", Event Type: " + addTrackingEventCommand.getEventType());

        } catch (Exception e) {
            logger.severe("ERROR storing tracking event for booking: " +
                    addTrackingEventCommand.getBookingId() + " - " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw para rollback transacional
        }
    }

}
