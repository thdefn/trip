package com.trip.diary.service;

import com.trip.diary.domain.model.Member;
import com.trip.diary.dto.MemberDto;
import com.trip.diary.elasticsearch.model.MemberDocument;
import com.trip.diary.elasticsearch.repository.MemberSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberSearchServiceTest {

    @Mock
    private MemberSearchRepository memberSearchRepository;
    @Mock
    private ElasticsearchOperations elasticsearchOperations;
    @Mock
    private ElasticsearchConverter elasticsearchConverter;
    @InjectMocks
    private MemberSearchService memberSearchService;

    Member member = Member.builder()
            .id(1L)
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .profilePath(null)
            .phone("01011111111")
            .build();

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