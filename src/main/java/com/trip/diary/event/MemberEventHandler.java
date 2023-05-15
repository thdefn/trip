package com.trip.diary.event;

import com.trip.diary.elasticsearch.model.MemberDocument;
import com.trip.diary.event.dto.MemberRegisterEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MemberEventHandler {
    private final ElasticsearchOperations elasticsearchOperations;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void addMemberToElasticSearch(MemberRegisterEvent event){
        elasticsearchOperations.save(MemberDocument.of(event.getMember()));
    }
}
