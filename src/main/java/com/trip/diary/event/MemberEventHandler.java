package com.trip.diary.event;

import com.trip.diary.event.dto.MemberRegisterEvent;
import com.trip.diary.service.MemberSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MemberEventHandler {
    private final MemberSearchService memberSearchService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberRegisterEvent(MemberRegisterEvent event) {
        memberSearchService.save(event.getMember());
    }
}
