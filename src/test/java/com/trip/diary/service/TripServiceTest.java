package com.trip.diary.service;

import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.domain.model.Location;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.dto.CreateTripDto;
import com.trip.diary.dto.CreateTripForm;
import com.trip.diary.dto.TripDto;
import com.trip.diary.dto.UpdateTripForm;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.TripException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {
    @Mock
    private TripRepository tripRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TripService tripService;

    Member member = Member.builder()
            .id(1L)
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .profilePath(null)
            .phone("01011111111")
            .build();

    Trip trip = Trip.builder()
            .id(1L)
            .title("임의의 타이틀")
            .isPrivate(true)
            .description("임의의 설명")
            .createdAt(LocalDateTime.now())
            .leader(member)
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

    @Test
    @DisplayName("여행 기록장 생성 성공")
    void createTest_success() {
        //given
        CreateTripForm form = CreateTripForm.builder()
                .title("제주도 여행팟")
                .description("제주도 여행을 떠나요")
                .isPrivate(true)
                .participants(new HashSet<>(Arrays.asList(2L, 3L)))
                .build();
        given(tripRepository.save(any())).willReturn(trip);
        given(memberRepository.findAllByIdIn(any()))
                .willReturn(List.of(participant1, participant2));
        given(participantRepository.saveAll(any()))
                .willReturn(List.of(Participant.builder()
                                .trip(trip)
                                .member(member)
                                .type(ParticipantType.ACCEPTED)
                                .build(),
                        Participant.builder()
                                .trip(trip)
                                .member(participant1)
                                .type(ParticipantType.PENDING)
                                .build(),
                        Participant.builder()
                                .trip(trip)
                                .member(participant2)
                                .type(ParticipantType.PENDING)
                                .build()
                ));
        //when
        CreateTripDto result = tripService.create(form, member);
        //then
        assertEquals(trip.getDescription(), result.getDescription());
        assertEquals(3, result.getParticipants().size());
        assertEquals(member.getId(), result.getParticipants().get(0).getId());
        assertNotEquals(member.getNickname(), result.getParticipants().get(0).getNickname());
        assertEquals(participant1.getNickname(), result.getParticipants().get(1).getNickname());
        assertFalse(result.getParticipants().get(1).getIsAccepted());
        assertFalse(result.getParticipants().get(1).getIsReader());
    }

    @Test
    @DisplayName("여행 기록장 생성 성공 - form 이 리더의 아이디도 포함한 경우")
    void createTest_successWhenParticipantsFormContainLeaderId() {
        //given
        CreateTripForm form = CreateTripForm.builder()
                .title("제주도 여행팟")
                .description("제주도 여행을 떠나요")
                .isPrivate(true)
                .participants(new HashSet<>(Arrays.asList(1L, 2L, 3L)))
                .build();
        given(tripRepository.save(any())).willReturn(trip);
        given(memberRepository.findAllByIdIn(any()))
                .willReturn(List.of(participant1, participant2));
        given(participantRepository.saveAll(any()))
                .willReturn(List.of(Participant.builder()
                                .trip(trip)
                                .member(member)
                                .type(ParticipantType.ACCEPTED)
                                .build(),
                        Participant.builder()
                                .trip(trip)
                                .member(participant1)
                                .type(ParticipantType.PENDING)
                                .build(),
                        Participant.builder()
                                .trip(trip)
                                .member(participant2)
                                .type(ParticipantType.PENDING)
                                .build()
                ));
        //when
        CreateTripDto result = tripService.create(form, member);
        //then
        assertEquals(trip.getDescription(), result.getDescription());
        assertEquals(3, result.getParticipants().size());
    }

    @Test
    @DisplayName("여행 기록장 수정 성공")
    void updateTripTest_success() {
        //given
        UpdateTripForm form = UpdateTripForm.builder()
                .title("강릉 여행팟")
                .description("강릉 여행을 5/13-5/20일 간다")
                .isPrivate(false)
                .build();
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(tripRepository.save(any()))
                .willReturn(Trip.builder()
                        .id(1L)
                        .title("타이틀입니다")
                        .isPrivate(true)
                        .description("설명입니다")
                        .participants(List.of(
                                Participant.builder()
                                        .trip(trip).member(member).build()
                        ))
                        .locations(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .leader(member)
                        .build());
        //when
        TripDto result = tripService.update(1L, form, member);
        //then
        verify(tripRepository, times(1)).save(any());
        assertEquals("타이틀입니다", result.getTitle());
        assertEquals("설명입니다", result.getDescription());
    }

    @Test
    @DisplayName("여행 기록장 수정 실패 - 해당 여행 기록장 없음")
    void updateTripTest_failWhenNotFoundTrip() {
        //given
        UpdateTripForm form = UpdateTripForm.builder()
                .title("강릉 여행팟")
                .description("강릉 여행을 5/13-5/20일 간다")
                .isPrivate(false)
                .build();
        given(tripRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        TripException exception = assertThrows(TripException.class,
                () -> tripService.update(1L, form, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_FOUND_TRIP);
    }

    @Test
    @DisplayName("여행 기록장 수정 실패 - 해당 여행 기록장 없음")
    void updateTripTest_failWhenNotAuthorityWriteTrip() {
        //given
        UpdateTripForm form = UpdateTripForm.builder()
                .title("강릉 여행팟")
                .description("강릉 여행을 5/13-5/20일 간다")
                .isPrivate(false)
                .build();
        Member leader = Member.builder()
                .id(2L)
                .username("asdfg")
                .nickname("이땡땡")
                .password("1234567")
                .profilePath(null)
                .phone("01011111112")
                .build();

        given(tripRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Trip.builder()
                                .id(1L)
                                .title("타이틀입니다")
                                .isPrivate(true)
                                .description("설명입니다")
                                .createdAt(LocalDateTime.now())
                                .leader(leader)
                                .build()
                ));
        //when
        TripException exception = assertThrows(TripException.class,
                () -> tripService.update(1L, form, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_AUTHORITY_WRITE_TRIP);
    }

    @Test
    @DisplayName("유저가 참여중인 여행 기록장 조회 성공")
    void readParticipatingTripTest_success() {
        //given
        Trip trip = Trip.builder()
                .id(1L)
                .title("경주가최고")
                .isPrivate(true)
                .description("6월 2일 경주로 여행을 갑니다")
                .leader(member)
                .createdAt(LocalDateTime.now())
                .participants(List.of(
                        Participant.builder()
                                .id(1L)
                                .type(ParticipantType.ACCEPTED)
                                .member(member)
                                .build()
                ))
                .locations(List.of(
                        Location.builder()
                                .id(3L)
                                .name("첨성대")
                                .build(),
                        Location.builder()
                                .id(4L)
                                .name("경주월드")
                                .build()
                ))
                .build();

        Trip trip2 = Trip.builder()
                .id(2L)
                .title("그래도 제주도")
                .isPrivate(true)
                .description("아휴 제주도가 짱이다 바다도 보고 한라산가고")
                .leader(member)
                .createdAt(LocalDateTime.now())
                .participants(List.of(
                        Participant.builder()
                                .id(1L)
                                .type(ParticipantType.ACCEPTED)
                                .member(member)
                                .build()
                ))
                .locations(List.of(
                        Location.builder()
                                .id(1L)
                                .name("제주공항")
                                .build(),
                        Location.builder()
                                .id(2L)
                                .name("김포공항")
                                .build()
                ))
                .build();

        given(participantRepository.findByMemberAndType(any(), any()))
                .willReturn(List.of(
                        Participant.builder()
                                .id(1L)
                                .trip(trip)
                                .member(member).build(),
                        Participant.builder()
                                .id(2L)
                                .trip(trip2)
                                .member(member).build()
                ));
        //when
        List<TripDto> result = tripService.readParticipatingTrip(member);
        //then
        assertEquals(1L, result.get(0).getId());
        assertEquals("경주가최고", result.get(0).getTitle());
        assertEquals("6월 2일 경주로 여행을 갑니다", result.get(0).getDescription());
        assertEquals(2, result.get(0).getLocations().size());
        assertEquals(1, result.get(0).getParticipants().size());
        assertEquals(2L, result.get(1).getId());
        assertEquals("그래도 제주도", result.get(1).getTitle());
        assertEquals("아휴 제주도가 짱이다 바다도 보고 한라산가고", result.get(1).getDescription());
        assertEquals(2, result.get(1).getLocations().size());
        assertEquals(1, result.get(1).getParticipants().size());

    }

}