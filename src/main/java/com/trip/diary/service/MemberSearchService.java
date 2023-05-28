package com.trip.diary.service;

import com.trip.diary.domain.model.Member;
import com.trip.diary.dto.MemberDto;
import com.trip.diary.elasticsearch.repository.MemberSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberSearchService {
    private final MemberSearchRepository memberSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private static final String INDEX_NAME_OF_MEMBER = "members";

    public void addTripIdToMemberDocument(Set<Long> participantsIds, Long tripId) {
        List<UpdateQuery> updateQueries = memberSearchRepository.findByIdIn(participantsIds)
                .stream().map(
                        memberDocument ->
                        {
                            memberDocument.addTripId(tripId);
                            return UpdateQuery.builder(memberDocument.getId().toString())
                                    .withDocument(elasticsearchOperations.getElasticsearchConverter().mapObject(memberDocument))
                                    .withDocAsUpsert(true)
                                    .build();
                        }
                ).collect(Collectors.toList());

        elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of(INDEX_NAME_OF_MEMBER));
    }

    public void removeTripIdToMemberDocument(Long memberId, Long tripId) {
        List<UpdateQuery> updateQueries = memberSearchRepository.findById(memberId)
                .stream().map(
                        memberDocument ->
                        {
                            memberDocument.removeTripId(tripId);
                            return UpdateQuery.builder(memberDocument.getId().toString())
                                    .withDocument(elasticsearchOperations.getElasticsearchConverter().mapObject(memberDocument))
                                    .withDocAsUpsert(true)
                                    .build();
                        }
                ).collect(Collectors.toList());

        elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of(INDEX_NAME_OF_MEMBER));
    }

    public List<MemberDto> searchAddableMembers(String keyword, Member member) {
        return memberSearchRepository.findByNicknameContainsIgnoreCase(keyword).stream()
                .filter(memberDocument -> !Objects.equals(memberDocument.getId(), member.getId()))
                .map(MemberDto::of)
                .collect(Collectors.toList());
    }

    public List<MemberDto> searchAddableMembersInTrip(Long tripId, String keyword, Member member) {
        return memberSearchRepository.findByNicknameContainsIgnoreCase(keyword).stream()
                .filter(memberDocument -> !Objects.equals(memberDocument.getId(), member.getId()))
                .map(memberDocument -> MemberDto.of(memberDocument, tripId))
                .collect(Collectors.toList());
    }
}
