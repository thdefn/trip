package com.trip.diary.service;

import com.trip.diary.client.ElasticSearchClient;
import com.trip.diary.domain.model.Member;
import com.trip.diary.dto.MemberDto;
import com.trip.diary.elasticsearch.model.MemberDocument;
import com.trip.diary.elasticsearch.repository.MemberSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberSearchService {
    private final MemberSearchRepository memberSearchRepository;
    private final ElasticSearchClient elasticSearchClient;
    private static final String INDEX_NAME_OF_MEMBER = "members";

    public void save(Member member) {
        elasticSearchClient.save(MemberDocument.from(member));
    }

    public void addTripIdToMemberDocument(Set<Long> participantsIds, Long tripId) {
        elasticSearchClient.update(INDEX_NAME_OF_MEMBER,
                memberSearchRepository.findByIdIn(participantsIds).stream()
                        .peek(memberDocument -> memberDocument.addTripId(tripId))
                        .collect(Collectors.toList()));
    }

    public void removeTripIdToMemberDocument(Long memberId, Long tripId) {
        memberSearchRepository.findById(memberId)
                .ifPresent(memberDocument -> {
                    memberDocument.removeTripId(tripId);
                    elasticSearchClient.update(INDEX_NAME_OF_MEMBER, memberDocument);
                });
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
