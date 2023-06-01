package com.trip.diary.event;

import com.trip.diary.event.dto.*;
import com.trip.diary.service.MemberSearchService;
import com.trip.diary.service.TripSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TripEventHandler {
    private final MemberSearchService memberSearchService;
    private final TripSearchService tripSearchService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripCreateEvent(TripCreateEvent event) {
        tripSearchService.save(event.getTrip());
        memberSearchService.addTripToMemberDocuments(event.getParticipantsIds(), event.getTrip().getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripUpdateEvent(TripUpdateEvent event) {
        tripSearchService.update(event.getTrip());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripInviteEvent(TripInviteEvent event) {
        memberSearchService.addTripToMemberDocuments(event.getParticipantsIds(), event.getTripId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripKickOutEvent(TripKickOutEvent event) {
        memberSearchService.removeTripToMemberDocument(event.getMemberId(), event.getTripId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLocationCreateEvent(LocationCreateEvent event) {
        tripSearchService.addLocationToTripDocument(event.getTripId(), event.getLocationName());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLocationDeleteEvent(LocationDeleteEvent event) {
        tripSearchService.removeToTripDocument(event.getTripId(), event.getLocationName());
    }
}
