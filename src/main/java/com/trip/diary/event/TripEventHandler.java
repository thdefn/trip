package com.trip.diary.event;

import com.trip.diary.elasticsearch.repository.MemberSearchRepository;
import com.trip.diary.event.dto.TripInviteEvent;
import com.trip.diary.event.dto.TripKickOutEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TripEventHandler {

    private final MemberSearchRepository memberSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void addTripIdToMemberDocument(TripInviteEvent event) {
        List<UpdateQuery> updateQueries = memberSearchRepository.findByIdIn(event.getParticipantsIds())
                .stream().map(
                        memberDocument ->
                        {
                            memberDocument.addTripId(event.getTripId());
                            return UpdateQuery.builder(memberDocument.getId().toString())
                                    .withDocument(elasticsearchOperations.getElasticsearchConverter().mapObject(memberDocument))
                                    .withDocAsUpsert(true)
                                    .build();
                        }
                ).collect(Collectors.toList());

        elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of("members"));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void removeTripIdToMemberDocument(TripKickOutEvent event) {
        List<UpdateQuery> updateQueries = memberSearchRepository.findById(event.getMemberId())
                .stream().map(
                        memberDocument ->
                        {
                            memberDocument.removeTripId(event.getTripId());
                            return UpdateQuery.builder(memberDocument.getId().toString())
                                    .withDocument(elasticsearchOperations.getElasticsearchConverter().mapObject(memberDocument))
                                    .withDocAsUpsert(true)
                                    .build();
                        }
                ).collect(Collectors.toList());

        elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of("members"));
    }
}
