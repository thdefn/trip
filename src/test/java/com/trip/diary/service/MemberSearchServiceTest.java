package com.trip.diary.service;

import com.trip.diary.client.ElasticSearchClient;
import com.trip.diary.domain.model.Member;
import com.trip.diary.dto.MemberDto;
import com.trip.diary.elasticsearch.model.MemberDocument;
import com.trip.diary.elasticsearch.repository.MemberSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberSearchServiceTest {

    @Mock
    private MemberSearchRepository memberSearchRepository;
    @Mock
    private ElasticSearchClient elasticSearchClient;
    @InjectMocks
    private MemberSearchService memberSearchService;

    Member member = Member.builder()
            .id(1L)
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .profilePath("profiles/basic.jpg")
            .phone("01011111111")
            .build();

    @Test
    @DisplayName("멤버 도큐먼트 저장 성공")
    void saveTest_success() {
        //given
        //when
        memberSearchService.save(member);
        ArgumentCaptor<MemberDocument> captor = ArgumentCaptor.forClass(MemberDocument.class);
        //then
        verify(elasticSearchClient, times(1)).save(captor.capture());
        assertEquals("김맹맹", captor.getValue().getNickname());
        assertEquals("profiles/basic.jpg", captor.getValue().getProfileUrl());
        assertEquals(0, captor.getValue().getTrips().size());
    }

    @Test
    @DisplayName("멤버 도큐먼트에 여행 기록장 추가 성공")
    void addTripToMemberDocumentsTest_success() {
        //given
        List<MemberDocument.Trip> trips = new ArrayList<>();
        trips.add(new MemberDocument.Trip(2L));
        trips.add(new MemberDocument.Trip(3L));

        given(memberSearchRepository.findByIdIn(anySet()))
                .willReturn(Arrays.asList(
                        MemberDocument.builder()
                                .id(1L)
                                .nickname("바밤바")
                                .profileUrl("basic.jpg")
                                .trips(trips)
                                .build()
                ));
        //when
        memberSearchService.addTripToMemberDocuments(Set.of(1L), 5L);
        ArgumentCaptor<List<MemberDocument>> captor = ArgumentCaptor.forClass(List.class);
        //then
        verify(elasticSearchClient, times(1)).update(anyString(), captor.capture());
        assertEquals(5L, captor.getValue().get(0).getTrips().get(2).getId());
    }

    @Test
    @DisplayName("멤버 도큐먼트에 여행 기록장 삭제 성공")
    void removeTripToMemberDocumentsTest_success() {
        //given
        List<MemberDocument.Trip> trips = new ArrayList<>();
        trips.add(new MemberDocument.Trip(2L));
        trips.add(new MemberDocument.Trip(3L));

        given(memberSearchRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        MemberDocument.builder()
                                .id(1L)
                                .nickname("바밤바")
                                .profileUrl("basic.jpg")
                                .trips(trips)
                                .build()
                ));
        //when
        memberSearchService.removeTripToMemberDocument(1L, 2L);
        ArgumentCaptor<MemberDocument> captor = ArgumentCaptor.forClass(MemberDocument.class);
        //then
        verify(elasticSearchClient, times(1)).update(anyString(), captor.capture());
        assertEquals(1, captor.getValue().getTrips().size());
        assertEquals(3L, captor.getValue().getTrips().get(0).getId());
    }

    @Test
    @DisplayName("여행 기록장에 초대할 멤버 검색 성공")
    void searchAddableMembersTest_success() {
        //given
        given(memberSearchRepository.findByNicknameContainsIgnoreCase(anyString()))
                .willReturn(Arrays.asList(
                        MemberDocument.builder()
                                .id(1L)
                                .nickname("바밤바")
                                .profileUrl("basic.jpg")
                                .trips(List.of(new MemberDocument.Trip(1L)))
                                .build(),
                        MemberDocument.builder()
                                .id(3L)
                                .nickname("투움바 파스타")
                                .profileUrl("basic.jpg")
                                .trips(List.of(
                                        new MemberDocument.Trip(2L),
                                        new MemberDocument.Trip(4L)
                                ))
                                .build(),
                        MemberDocument.builder()
                                .id(4L)
                                .nickname("바나나")
                                .profileUrl("basic.jpg")
                                .trips(List.of(new MemberDocument.Trip(1L)))
                                .build()
                ));
        //when
        List<MemberDto> result = memberSearchService.searchAddableMembers("바", member);
        //then
        assertEquals(2, result.size());
        assertEquals(result.get(0).getId(), 3L);
        assertEquals(result.get(0).getNickname(), "투움바 파스타");
        assertEquals(result.get(1).getId(), 4L);
        assertEquals(result.get(1).getNickname(), "바나나");
    }

    @Test
    @DisplayName("생성된 여행 기록장에 초대할 멤버 검색 성공")
    void searchAddableMembersInTripTest_success() {
        //given
        given(memberSearchRepository.findByNicknameContainsIgnoreCase(anyString()))
                .willReturn(Arrays.asList(
                        MemberDocument.builder()
                                .id(1L)
                                .nickname("바밤바")
                                .profileUrl("basic.jpg")
                                .trips(List.of(new MemberDocument.Trip(1L)))
                                .build(),
                        MemberDocument.builder()
                                .id(3L)
                                .nickname("투움바 파스타")
                                .profileUrl("basic.jpg")
                                .trips(List.of(
                                        new MemberDocument.Trip(2L),
                                        new MemberDocument.Trip(4L)
                                ))
                                .build(),
                        MemberDocument.builder()
                                .id(4L)
                                .nickname("바나나")
                                .profileUrl("basic.jpg")
                                .trips(List.of(new MemberDocument.Trip(1L)))
                                .build()
                ));
        //when
        List<MemberDto> result = memberSearchService.searchAddableMembersInTrip(1L, "바", member);
        //then
        assertEquals(2, result.size());
        assertFalse(result.get(0).getIsInvited());
        assertTrue(result.get(1).getIsInvited());
        assertEquals(result.get(0).getId(), 3L);
        assertEquals(result.get(0).getNickname(), "투움바 파스타");
        assertEquals(result.get(1).getId(), 4L);
        assertEquals(result.get(1).getNickname(), "바나나");
    }

}