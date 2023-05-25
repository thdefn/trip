package com.trip.diary.service;

import com.trip.diary.domain.model.*;
import com.trip.diary.domain.repository.LocationRepository;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.dto.LocationDetailDto;
import com.trip.diary.dto.LocationDto;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.TripException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {
    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private LocationService locationService;

    Member member = Member.builder()
            .id(1L)
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .profilePath(null)
            .phone("01011111111")
            .build();

    Member participant1 = Member.builder()
            .id(2L)
            .username("abcde")
            .nickname("하이")
            .profilePath(null)
            .build();
    Member participant2 = Member.builder()
            .id(3L)
            .username("abcde")
            .nickname("하이")
            .profilePath(null)
            .build();

    Trip trip = Trip.builder()
            .id(1L)
            .title("임의의 타이틀")
            .isPrivate(true)
            .description("임의의 설명")
            .leader(member)
            .participants(List.of(
                    Participant.builder()
                            .member(member)
                            .type(ParticipantType.ACCEPTED)
                            .build(),
                    Participant.builder()
                            .member(participant1)
                            .type(ParticipantType.ACCEPTED)
                            .build(),
                    Participant.builder()
                            .member(participant2)
                            .type(ParticipantType.ACCEPTED)
                            .build()
            ))
            .build();

    @Test
    @DisplayName("로케이션 상세 리스트 읽기 성공")
    void readLocationDetailsTest_success() {
        //given
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(true);
        given(locationRepository.findByTripOrderByIdDesc(trip))
                .willReturn(List.of(
                        Location.builder()
                                .id(1L)
                                .name("제주공항")
                                .thumbnailPath("/posts/1.jpg")
                                .posts(List.of(
                                        Post.builder()
                                                .id(1L)
                                                .content("제주공항에서 본 고양이 짱귀엽다")
                                                .member(participant1)
                                                .images(
                                                        List.of(PostImage.builder()
                                                                .id(1L)
                                                                .imagePath("/posts/1.jpg")
                                                                .build()
                                                        )
                                                )
                                                .build(),
                                        Post.builder()
                                                .id(2L)
                                                .content("제주도 첫끼니는 버거킹..ㅋ")
                                                .member(participant1)
                                                .images(
                                                        List.of(PostImage.builder()
                                                                        .id(2L)
                                                                        .imagePath("/posts/2.jpg")
                                                                        .build(),
                                                                PostImage.builder()
                                                                        .id(2L)
                                                                        .imagePath("/posts/3.jpg")
                                                                        .build()
                                                        )
                                                )
                                                .build()
                                ))
                                .build()
                ));
        //when
        List<LocationDetailDto> result = locationService.readLocationDetails(1L, member);
        //then
        assertEquals(1L, result.get(0).getId());
        assertEquals("제주공항", result.get(0).getName());
        assertEquals(1, result.get(0).getProfilePaths().size());
        assertEquals(3, result.get(0).getPosts().size());
        assertEquals(1L, result.get(0).getPosts().get(0).getId());
        assertEquals("/posts/1.jpg", result.get(0).getPosts().get(0).getImagePath());
        assertEquals(2L, result.get(0).getPosts().get(1).getId());
        assertEquals("/posts/2.jpg", result.get(0).getPosts().get(1).getImagePath());
        assertEquals(2L, result.get(0).getPosts().get(2).getId());
        assertEquals("/posts/3.jpg", result.get(0).getPosts().get(2).getImagePath());
    }

    @Test
    @DisplayName("로케이션 상세 리스트 읽기 실패 - 해당 여행 기록장 없음")
    void readLocationDetailsTest_failWhenNotFoundTrip() {
        //given
        given(tripRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        TripException exception = assertThrows(TripException.class, () -> locationService.readLocationDetails(1L, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("로케이션 상세 리스트 읽기 실패 - 해당 여행 기록장에 대한 참여자가 아님")
    void readLocationDetailsTest_failWhenNotAuthorityReadTrip() {
        //given
        Trip trip = Trip.builder()
                .id(1L)
                .title("임의의 타이틀")
                .isPrivate(true)
                .description("임의의 설명")
                .leader(participant1)
                .participants(List.of(
                        Participant.builder()
                                .member(participant1)
                                .type(ParticipantType.ACCEPTED)
                                .build(),
                        Participant.builder()
                                .member(participant2)
                                .type(ParticipantType.ACCEPTED)
                                .build()
                ))
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(false);
        //when
        TripException exception = assertThrows(TripException.class, () -> locationService.readLocationDetails(1L, member));
        //then
        assertEquals(ErrorCode.NOT_AUTHORITY_READ_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("로케이션 리스트 읽기 성공")
    void readLocationsTest_success() {
        //given
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(true);
        given(locationRepository.findByTripOrderByIdDesc(trip))
                .willReturn(List.of(
                        Location.builder()
                                .id(1L)
                                .name("제주공항")
                                .thumbnailPath("/posts/1.jpg")
                                .posts(List.of(
                                        Post.builder()
                                                .id(1L)
                                                .content("제주공항에서 본 고양이 짱귀엽다")
                                                .member(participant1)
                                                .images(
                                                        List.of(PostImage.builder()
                                                                .id(1L)
                                                                .imagePath("/posts/1.jpg")
                                                                .build()
                                                        )
                                                )
                                                .build(),
                                        Post.builder()
                                                .id(2L)
                                                .content("제주도 첫끼니는 버거킹..ㅋ")
                                                .member(participant1)
                                                .images(
                                                        List.of(PostImage.builder()
                                                                        .id(2L)
                                                                        .imagePath("/posts/2.jpg")
                                                                        .build(),
                                                                PostImage.builder()
                                                                        .id(2L)
                                                                        .imagePath("/posts/3.jpg")
                                                                        .build()
                                                        )
                                                )
                                                .build()
                                ))
                                .build()
                ));
        //when
        List<LocationDto> result = locationService.readLocations(1L, member);
        //then
        assertEquals(1L, result.get(0).getId());
        assertEquals("제주공항", result.get(0).getName());
        assertEquals("/posts/1.jpg", result.get(0).getThumbnailPath());
        assertEquals(3, result.get(0).getNumbersOfImages());
    }

    @Test
    @DisplayName("로케이션 리스트 읽기 실패 - 해당 여행 기록장 없음")
    void readLocationsTest_failWhenNotFoundTrip() {
        //given
        given(tripRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        TripException exception = assertThrows(TripException.class, () -> locationService.readLocations(1L, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("로케이션 리스트 읽기 실패 - 해당 여행 기록장에 대한 참여자가 아님")
    void readLocationsTest_failWhenNotAuthorityReadTrip() {
        //given
        Trip trip = Trip.builder()
                .id(1L)
                .title("임의의 타이틀")
                .isPrivate(true)
                .description("임의의 설명")
                .leader(participant1)
                .participants(List.of(
                        Participant.builder()
                                .member(participant1)
                                .type(ParticipantType.ACCEPTED)
                                .build(),
                        Participant.builder()
                                .member(participant2)
                                .type(ParticipantType.ACCEPTED)
                                .build()
                ))
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(false);
        //when
        TripException exception = assertThrows(TripException.class,
                () -> locationService.readLocations(1L, member));
        //then
        assertEquals(ErrorCode.NOT_AUTHORITY_READ_TRIP, exception.getErrorCode());
    }

}