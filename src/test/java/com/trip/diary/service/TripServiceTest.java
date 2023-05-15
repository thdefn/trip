package com.trip.diary.service;

import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.domain.type.ParticipantType;
import com.trip.diary.dto.*;
import com.trip.diary.exception.CustomException;
import com.trip.diary.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TripService tripService;

    Member member = Member.builder()
            .id(1L)
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .profileUrl(null)
            .phone("01011111111")
            .build();

    Trip trip = Trip.builder()
            .id(1L)
            .title("임의의 타이틀")
            .isPrivate(true)
            .description("임의의 설명")
            .leader(member)
            .build();

    Member participant1 = Member.builder()
            .id(2L)
            .username("abcde")
            .nickname("하이")
            .profileUrl(null)
            .build();
    Member participant2 = Member.builder()
            .id(3L)
            .username("abcde")
            .nickname("하이")
            .profileUrl(null)
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
        assertFalse(result.getParticipants().get(1).isAccepted());
        assertFalse(result.getParticipants().get(1).isReader());
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
                        .leader(member)
                        .build());
        //when
        TripDto result = tripService.updateTrip(1L, form, member);
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
        CustomException exception = assertThrows(CustomException.class,
                () -> tripService.updateTrip(1L, form, member));
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
                .profileUrl(null)
                .phone("01011111112")
                .build();

        given(tripRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Trip.builder()
                                .id(1L)
                                .title("타이틀입니다")
                                .isPrivate(true)
                                .description("설명입니다")
                                .leader(leader)
                                .build()
                ));
        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> tripService.updateTrip(1L, form, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_AUTHORITY_WRITE_TRIP);
    }

    @Test
    @DisplayName("여행 기록장 참여 멤버 조회 성공 - 조회자가 리더인 경우")
    void getTripParticipantsTest_successWhenReaderIsTripLeader() {
        //given
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
                                .type(ParticipantType.PENDING)
                                .build(),
                        Participant.builder()
                                .member(participant2)
                                .type(ParticipantType.PENDING)
                                .build()))
                .build();
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        //when
        List<ParticipantDto> result = tripService.getTripParticipants(1L, member);
        //then
        assertTrue(result.get(0).isReader());
        assertTrue(result.get(0).isAccepted());
        assertEquals(participant1.getNickname(), result.get(1).getNickname());
        assertFalse(result.get(1).isReader());
        assertFalse(result.get(1).isAccepted());
    }

    @Test
    @DisplayName("여행 기록장 참여 멤버 조회 성공 - 조회자가 초대받은 사람인 경우")
    void getTripParticipantsTest_successWhenReaderIsParticipants() {
        //given
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
                                .type(ParticipantType.PENDING)
                                .build(),
                        Participant.builder()
                                .member(participant2)
                                .type(ParticipantType.PENDING)
                                .build()))
                .build();
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        //when
        List<ParticipantDto> result = tripService.getTripParticipants(1L, participant1);
        //then
        assertFalse(result.get(0).isReader());
        assertTrue(result.get(0).isAccepted());
        assertNotEquals(participant1.getNickname(), result.get(1).getNickname());
        assertTrue(result.get(1).isReader());
        assertFalse(result.get(1).isAccepted());
    }

    @Test
    @DisplayName("여행 기록장 참여 멤버 조회 성공 - 조회자가 참여자가 아니지만 해당 여행기록장이 모두 공개임")
    void getTripParticipantsTest_successWhenTripIsPublic() {
        //given
        Member leader = Member.builder()
                .id(4L)
                .username("1234qwert")
                .nickname("오땡땡")
                .password("1234567")
                .profileUrl(null)
                .phone("01011111114")
                .build();

        Trip trip = Trip.builder()
                .id(1L)
                .title("임의의 타이틀")
                .isPrivate(false)
                .description("임의의 설명")
                .leader(leader)
                .participants(List.of(
                        Participant.builder()
                                .member(leader)
                                .type(ParticipantType.ACCEPTED)
                                .build(),
                        Participant.builder()
                                .member(participant1)
                                .type(ParticipantType.PENDING)
                                .build(),
                        Participant.builder()
                                .member(participant2)
                                .type(ParticipantType.PENDING)
                                .build()))
                .build();
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        //when
        List<ParticipantDto> result = tripService.getTripParticipants(1L, member);
        //then
        assertFalse(result.get(0).isReader());
        assertTrue(result.get(0).isAccepted());
        assertEquals(participant1.getNickname(), result.get(1).getNickname());
        assertFalse(result.get(1).isReader());
        assertFalse(result.get(1).isAccepted());
    }

    @Test
    @DisplayName("여행 기록장 참여 멤버 조회 성공 - 조회자가 참여자가 아니고 해당 여행 기록장이 비밀임")
    void getTripParticipantsTest_failWhenNotAuthorityReadTrip() {
        //given
        Member leader = Member.builder()
                .id(4L)
                .username("1234qwert")
                .nickname("오땡땡")
                .password("1234567")
                .profileUrl(null)
                .phone("01011111114")
                .build();

        Trip trip = Trip.builder()
                .id(1L)
                .title("임의의 타이틀")
                .isPrivate(true)
                .description("임의의 설명")
                .leader(leader)
                .participants(List.of(
                        Participant.builder()
                                .member(leader)
                                .type(ParticipantType.ACCEPTED)
                                .build(),
                        Participant.builder()
                                .member(participant1)
                                .type(ParticipantType.PENDING)
                                .build(),
                        Participant.builder()
                                .member(participant2)
                                .type(ParticipantType.PENDING)
                                .build()))
                .build();
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> tripService.getTripParticipants(1L, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_AUTHORITY_READ_TRIP);
    }

}