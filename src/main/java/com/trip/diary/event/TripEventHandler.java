package com.trip.diary.event;

import com.trip.diary.event.dto.TripInviteEvent;
import com.trip.diary.event.dto.TripKickOutEvent;
import com.trip.diary.service.MemberSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TripEventHandler {

    private final MemberSearchService memberSearchService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripInviteEvent(TripInviteEvent event) {
        memberSearchService.addTripIdToMemberDocument(event.getParticipantsIds(), event.getTripId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripKickOutEvent(TripKickOutEvent event) {
        memberSearchService.removeTripIdToMemberDocument(event.getMemberId(), event.getTripId());
    }
}
