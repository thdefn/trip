package com.trip.diary.service;

import com.trip.diary.client.ElasticSearchClient;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.BookmarkRepository;
import com.trip.diary.domain.repository.LocationRepositoryCustom;
import com.trip.diary.domain.repository.TripRepositoryCustom;
import com.trip.diary.dto.TripBookmarkDto;
import com.trip.diary.dto.TripDto;
import com.trip.diary.elasticsearch.model.TripDocument;
import com.trip.diary.elasticsearch.repository.TripSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TripSearchServiceTest {
    @Mock
    private TripSearchRepository tripSearchRepository;

    @Mock
    private LocationRepositoryCustom locationRepositoryCustom;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private TripRepositoryCustom tripRepositoryCustom;

    @Mock
    private ElasticSearchClient elasticSearchClient;

    @InjectMocks
    private TripSearchService tripSearchService;

    Member member = Member.builder()
            .id(1L)
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .profilePath("profiles/basic.jpg")
            .phone("01011111111")
            .build();

    @Test
    @DisplayName("여행 기록장 도큐먼트 저장 성공")
    void saveTest_success() {
        //given
        Trip trip = Trip.builder()
                .id(1L)
                .title("임의의 타이틀")
                .isPrivate(true)
                .description("임의의 설명")
                .leader(member)
                .build();
        //when
        tripSearchService.save(trip);
        ArgumentCaptor<TripDocument> captor = ArgumentCaptor.forClass(TripDocument.class);
        //then
        verify(elasticSearchClient, times(1)).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertTrue(captor.getValue().isPrivate());
        assertEquals("임의의 타이틀", captor.getValue().getTitle());
        assertEquals("임의의 설명", captor.getValue().getDescription());
    }

    @Test
    @DisplayName("여행 기록장 도큐먼트 수정 성공")
    void updateTest_success() {
        //given
        Trip trip = Trip.builder()
                .id(1L)
                .title("괌으로 가잣")
                .isPrivate(true)
                .description("제주도보다는 괌이짓")
                .leader(member)
                .build();

        given(tripSearchRepository.findById(anyLong()))
                .willReturn(Optional.of(TripDocument.builder()
                        .id(1L)
                        .title("제주여행")
                        .description("제주도가 최고")
                        .isPrivate(false)
                        .build()
                ));
        //when
        tripSearchService.update(trip);
        ArgumentCaptor<TripDocument> captor = ArgumentCaptor.forClass(TripDocument.class);
        //then
        verify(elasticSearchClient, times(1)).update(anyString(), captor.capture());
        assertEquals("괌으로 가잣", captor.getValue().getTitle());
        assertEquals("제주도보다는 괌이짓", captor.getValue().getDescription());
        assertTrue(captor.getValue().isPrivate());
    }

    @Test
    @DisplayName("여행 기록장 도큐먼트에 로케이션 추가 성공")
    void addLocationToTripDocumentTest_success() {
        //given
        given(tripSearchRepository.findById(anyLong()))
                .willReturn(Optional.of(TripDocument.builder()
                        .id(1L)
                        .title("제주여행")
                        .description("제주도가 최고")
                        .isPrivate(false)
                        .build()
                ));
        //when
        tripSearchService.addLocationToTripDocument(1L, "제주 올레길");
        ArgumentCaptor<TripDocument> captor = ArgumentCaptor.forClass(TripDocument.class);
        //then
        verify(elasticSearchClient, times(1)).update(anyString(), captor.capture());
        assertEquals(1, captor.getValue().getLocations().size());
        assertEquals("제주 올레길", captor.getValue().getLocations().get(0).getName());
    }

    @Test
    @DisplayName("여행 기록장 도큐먼트에 로케이션 삭제 성공")
    void removeToTripDocumentTest_success() {
        //given
        List<TripDocument.Location> locations = new ArrayList<>();
        locations.add(new TripDocument.Location("제주 올레길"));
        given(tripSearchRepository.findById(anyLong()))
                .willReturn(Optional.of(TripDocument.builder()
                        .id(1L)
                        .title("제주여행")
                        .description("제주도가 최고")
                        .isPrivate(false)
                        .build()
                ));
        //when
        tripSearchService.removeToTripDocument(1L, "제주 올레길");
        ArgumentCaptor<TripDocument> captor = ArgumentCaptor.forClass(TripDocument.class);
        //then
        verify(elasticSearchClient, times(1)).update(anyString(), captor.capture());
        assertEquals(0, captor.getValue().getLocations().size());
    }

    @Test
    @DisplayName("여행 기록장 검색 성공")
    void searchTest_success() {
        //given
        List<TripDocument.Location> locations = new ArrayList<>();
        locations.add(new TripDocument.Location("경주월드"));
        locations.add(new TripDocument.Location("안압지"));
        locations.add(new TripDocument.Location("경주월드"));
        given(tripSearchRepository.findByKeyword(anyString(), anyString(), any()))
                .willReturn(new PageImpl<>(
                        List.of(TripDocument.builder()
                                .id(3L)
                                .title("경주여행")
                                .description("경주가 최고")
                                .locations(locations)
                                .isPrivate(false)
                                .build())
                ));
        given(bookmarkRepository.existsByTrip_IdAndMember(anyLong(), any()))
                .willReturn(false);
        //when
        Page<TripDto> result = tripSearchService.search(0, "경주", member);
        //then
        assertEquals(3L, result.getContent().get(0).getId());
        assertEquals("경주여행", result.getContent().get(0).getTitle());
        assertEquals("경주가 최고", result.getContent().get(0).getDescription());
        assertFalse(result.getContent().get(0).getIsBookmarked());
        assertEquals(2, result.getContent().get(0).getLocations().size());
    }

    @Test
    @DisplayName("여행 기록장 로케이션으로 검색 성공")
    void searchByLocationTest_success() {
        //given
        List<TripDocument.Location> locations = new ArrayList<>();
        locations.add(new TripDocument.Location("경주월드"));
        locations.add(new TripDocument.Location("안압지"));
        locations.add(new TripDocument.Location("경주월드"));
        given(tripSearchRepository.findByLocationsName(anyString(), any()))
                .willReturn(new PageImpl<>(
                        List.of(TripDocument.builder()
                                .id(3L)
                                .title("경주여행")
                                .description("경주가 최고")
                                .locations(locations)
                                .isPrivate(false)
                                .build())
                ));
        given(bookmarkRepository.existsByTrip_IdAndMember(anyLong(), any()))
                .willReturn(false);
        //when
        Page<TripDto> result = tripSearchService.searchByLocation(0, "경주월드", member);
        //then
        assertEquals(3L, result.getContent().get(0).getId());
        assertEquals("경주여행", result.getContent().get(0).getTitle());
        assertEquals("경주가 최고", result.getContent().get(0).getDescription());
        assertFalse(result.getContent().get(0).getIsBookmarked());
        assertEquals(2, result.getContent().get(0).getLocations().size());
    }

    @Test
    @DisplayName("여행 기록장 북마크 순으로 검색 성공")
    void searchByKeywordOrderByBookmarkTest_success() {
        //given
        given(tripRepositoryCustom.findByKeywordContainsOrderByBookmark(anyString(), any()))
                .willReturn(new PageImpl<>(
                        List.of(
                                TripBookmarkDto.builder()
                                        .tripId(1L)
                                        .title("제주도")
                                        .isPrivate(false)
                                        .description("제주도입니닷")
                                        .countOfBookmarked(3L)
                                        .build(),
                                TripBookmarkDto.builder()
                                        .tripId(2L)
                                        .title("제주도 여행기")
                                        .isPrivate(false)
                                        .description("5명이서 제주도간다")
                                        .countOfBookmarked(1L)
                                        .build()
                        )
                ));
        given(bookmarkRepository.existsByTrip_IdAndMember(1L, member))
                .willReturn(true);
        given(bookmarkRepository.existsByTrip_IdAndMember(2L, member))
                .willReturn(false);
        given(locationRepositoryCustom.findLocationNameByTripId(1L))
                .willReturn(List.of("한라산", "제주공항"));
        given(locationRepositoryCustom.findLocationNameByTripId(2L))
                .willReturn(List.of("우도", "올레길"));
        //when
        Page<TripDto> result = tripSearchService.searchByKeywordOrderByBookmark(0, "제주도", member);
        //then
        assertEquals(3L, result.getContent().get(0).getCountOfBookmark());
        assertTrue(result.getContent().get(0).getIsBookmarked());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals("제주도", result.getContent().get(0).getTitle());
        assertEquals("제주도입니닷", result.getContent().get(0).getDescription());
        assertEquals(1L, result.getContent().get(1).getCountOfBookmark());
        assertFalse(result.getContent().get(1).getIsBookmarked());
        assertEquals(2L, result.getContent().get(1).getId());
        assertEquals("제주도 여행기", result.getContent().get(1).getTitle());
        assertEquals("5명이서 제주도간다", result.getContent().get(1).getDescription());
    }

}