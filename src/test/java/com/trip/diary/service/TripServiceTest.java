package com.trip.diary.service;

import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.dto.*;
import com.trip.diary.elasticsearch.model.MemberDocument;
import com.trip.diary.elasticsearch.repository.MemberSearchRepository;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.MemberException;
import com.trip.diary.exception.TripException;
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
import static org.mockito.ArgumentMatchers.*;
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
            .profilePath(null)
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
        TripException exception = assertThrows(TripException.class,
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
                                .leader(leader)
                                .build()
                ));
        //when
        TripException exception = assertThrows(TripException.class,
                () -> tripService.updateTrip(1L, form, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_AUTHORITY_WRITE_TRIP);
    }

    @Test
    @DisplayName("여행 기록장에 멤버 초대 성공")
    void invite_success() {
        //given
        Member target = Member.builder()
                .id(4L)
                .username("1234qwert")
                .nickname("오땡땡")
                .password("1234567")
                .profilePath(null)
                .phone("01011111114")
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
                                .type(ParticipantType.PENDING)
                                .build(),
                        Participant.builder()
                                .member(participant2)
                                .type(ParticipantType.PENDING)
                                .build()))
                .build();
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(target));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(true);
        given(participantRepository.existsByTripAndMember(any(), any())).willReturn(false);
        //when
        tripService.invite(1L, 4L, member);
        //then
        verify(participantRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("여행 기록장에 멤버 초대 실패 - 이미 초대된 유저임")
    void invite_failWhenUserAlreadyInvited() {
        //given
        Member target = Member.builder()
                .id(4L)
                .username("1234qwert")
                .nickname("오땡땡")
                .password("1234567")
                .profilePath(null)
                .phone("01011111114")
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
                                .type(ParticipantType.PENDING)
                                .build(),
                        Participant.builder()
                                .member(participant2)
                                .type(ParticipantType.PENDING)
                                .build()))
                .build();
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(target));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(true);
        given(participantRepository.existsByTripAndMember(any(), any())).willReturn(true);
        //when
        TripException exception = assertThrows(TripException.class,
                () -> tripService.invite(1L, 4L, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.USER_ALREADY_INVITED);
    }

    @Test
    @DisplayName("여행 기록장에 멤버 초대 실패 - 해당 여행 기록장 없음")
    void invite_failWhenNotFoundTrip() {
        //given
        given(tripRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        TripException exception = assertThrows(TripException.class,
                () -> tripService.invite(1L, 4L, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_FOUND_TRIP);
    }

    @Test
    @DisplayName("여행 기록장에 멤버 초대 실패 - 해당 유저 없음")
    void invite_failWhenNotFoundMember() {
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
        given(memberRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> tripService.invite(1L, 4L, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_FOUND_MEMBER);
    }

    @Test
    @DisplayName("여행 기록장에 멤버 초대 실패 - 해당 권한 없음")
    void invite_failWhenNotAuthorityWriteTrip() {
        //given
        Member target = Member.builder()
                .id(4L)
                .username("1234qwert")
                .nickname("오땡땡")
                .password("1234567")
                .profilePath(null)
                .phone("01011111114")
                .build();

        Trip trip = Trip.builder()
                .id(1L)
                .title("임의의 타이틀")
                .isPrivate(true)
                .description("임의의 설명")
                .leader(participant1)
                .participants(List.of(
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
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(target));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(false);
        //when
        TripException exception = assertThrows(TripException.class,
                () -> tripService.invite(1L, 4L, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_AUTHORITY_WRITE_TRIP);
    }

    @Test
    @DisplayName("여행 기록장에 멤버 퇴출 성공")
    void kickOut_success() {
        //given
        Member target = Member.builder()
                .id(4L)
                .username("1234qwert")
                .nickname("오땡땡")
                .password("1234567")
                .profilePath(null)
                .phone("01011111114")
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
                                .type(ParticipantType.PENDING)
                                .build(),
                        Participant.builder()
                                .member(participant2)
                                .type(ParticipantType.PENDING)
                                .build()))
                .build();
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(participantRepository.findByTripAndMember_Id(any(), anyLong()))
                .willReturn(Optional.of(Participant.builder()
                        .member(participant2)
                        .type(ParticipantType.PENDING)
                        .build()
                ));
        //when
        tripService.kickOut(1L, 4L, member);
        //then
        verify(participantRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("여행 기록장에 멤버 퇴출 실패 - 해당 여행 기록장 없음")
    void kickOut_failWhenNotFoundTrip() {
        //given
        given(tripRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        TripException exception = assertThrows(TripException.class,
                () -> tripService.invite(1L, 4L, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_FOUND_TRIP);
    }

    @Test
    @DisplayName("여행 기록장에 멤버 퇴출 실패 - 해당 유저 없음")
    void kickOut_failWhenNotFoundMember() {
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
        given(participantRepository.findByTripAndMember_Id(any(), anyLong()))
                .willReturn(Optional.empty());
        //when
        TripException exception = assertThrows(TripException.class,
                () -> tripService.kickOut(1L, 4L, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_FOUND_PARTICIPANT);
    }

    @Test
    @DisplayName("여행 기록장에 멤버 퇴출 실패 - 해당 권한 없음")
    void kickOut_failWhenNotAuthorityWriteTrip() {
        //given
        Member target = Member.builder()
                .id(4L)
                .username("1234qwert")
                .nickname("오땡땡")
                .password("1234567")
                .profilePath(null)
                .phone("01011111114")
                .build();

        Trip trip = Trip.builder()
                .id(1L)
                .title("임의의 타이틀")
                .isPrivate(true)
                .description("임의의 설명")
                .leader(participant1)
                .participants(List.of(
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
        TripException exception = assertThrows(TripException.class,
                () -> tripService.kickOut(1L, 4L, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_AUTHORITY_WRITE_TRIP);
    }

}