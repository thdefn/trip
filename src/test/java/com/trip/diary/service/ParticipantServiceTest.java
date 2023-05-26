package com.trip.diary.service;

import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.dto.ParticipantDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private ParticipantService participantService;

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
        given(participantRepository.existsByTripAndMember(any(), any())).willReturn(true);
        //when
        List<ParticipantDto> result = participantService.getTripParticipants(1L, member);
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
        given(participantRepository.existsByTripAndMember(any(), any())).willReturn(true);
        //when
        List<ParticipantDto> result = participantService.getTripParticipants(1L, participant1);
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
                .profilePath(null)
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
        List<ParticipantDto> result = participantService.getTripParticipants(1L, member);
        //then
        assertFalse(result.get(0).isReader());
        assertTrue(result.get(0).isAccepted());
        assertEquals(participant1.getNickname(), result.get(1).getNickname());
        assertFalse(result.get(1).isReader());
        assertFalse(result.get(1).isAccepted());
    }

    @Test
    @DisplayName("여행 기록장 참여 멤버 조회 실패 - 조회자가 참여자가 아니고 해당 여행 기록장이 비밀임")
    void getTripParticipantsTest_failWhenNotAuthorityReadTrip() {
        //given
        Member leader = Member.builder()
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
        given(participantRepository.existsByTripAndMember(any(), any())).willReturn(false);
        //when
        TripException exception = assertThrows(TripException.class,
                () -> participantService.getTripParticipants(1L, member));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.NOT_AUTHORITY_READ_TRIP);
    }

}