package com.trip.diary.service;

import com.trip.diary.client.ElasticSearchClient;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.elasticsearch.model.TripDocument;
import com.trip.diary.elasticsearch.repository.TripSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TripSearchServiceTest {
    @Mock
    private TripSearchRepository tripSearchRepository;

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

}