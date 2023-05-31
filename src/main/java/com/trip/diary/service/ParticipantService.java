package com.trip.diary.service;

import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.dto.ParticipantDto;
import com.trip.diary.dto.TripDto;
import com.trip.diary.event.dto.TripInviteEvent;
import com.trip.diary.event.dto.TripKickOutEvent;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.MemberException;
import com.trip.diary.exception.ParticipantException;
import com.trip.diary.exception.TripException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.trip.diary.domain.constants.ParticipantType.ACCEPTED;
import static com.trip.diary.domain.constants.ParticipantType.PENDING;
import static com.trip.diary.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final TripRepository tripRepository;
    private final ParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public List<ParticipantDto> getTripParticipants(Long tripId, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        List<Participant> participants = trip.getParticipants();

        if (trip.isPrivate() && !participantRepository.existsByTripAndMember(trip, member)) {
            throw new TripException(ErrorCode.NOT_AUTHORITY_READ_TRIP);
        }

        return participants
                .stream()
                .map(participant ->
                        ParticipantDto.of(participant, member.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TripDto> getInvitedTripList(Member member) {
        return participantRepository.findByMemberAndType(member, ParticipantType.PENDING)
                .stream().map(participant -> TripDto.of(participant.getTrip()))
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(key = "{#tripId, #member.id}", value = "TripAuthorities")
    public void acceptTripInvitation(Long tripId, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        Participant participant =
                participantRepository.findByTripAndMemberAndType(trip, member, ParticipantType.PENDING)
                        .orElseThrow(() -> new ParticipantException(NOT_INVITED_TRIP));
        participant.setAccepted();
    }

    @Transactional
    public void denyTripInvitation(Long tripId, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        Participant participant =
                participantRepository.findByTripAndMemberAndType(trip, member, ParticipantType.PENDING)
                        .orElseThrow(() -> new ParticipantException(NOT_INVITED_TRIP));
        participantRepository.delete(participant);
        applicationEventPublisher.publishEvent(new TripKickOutEvent(member.getId(), tripId));
    }

    @Transactional
    public void invite(Long tripId, Long targetMemberId, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        Member target = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        validationMemberHaveWriteAuthority(trip, member);

        if (participantRepository.existsByTripAndMember(trip, target)) {
            throw new TripException(USER_ALREADY_INVITED);
        }

        participantRepository.save(Participant.builder()
                .member(target)
                .trip(trip)
                .type(PENDING)
                .build());
        notificationService.notifyInvitation(trip.getTitle(), member.getNickname(), Set.of(targetMemberId));
        applicationEventPublisher.publishEvent(new TripInviteEvent(Set.of(targetMemberId), trip.getId()));
    }

    private void validationMemberHaveWriteAuthority(Trip trip, Member member) {
        if (!participantRepository.existsByTripAndMemberAndType(trip, member, ACCEPTED)) {
            throw new TripException(NOT_AUTHORITY_WRITE_TRIP);
        }
    }

    @Transactional
    @CacheEvict(key = "{#tripId, #targetId}", value = "TripAuthorities")
    public void kickOut(Long tripId, Long targetId, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        if (!Objects.equals(trip.getLeader().getId(), member.getId())) {
            throw new TripException(NOT_AUTHORITY_WRITE_TRIP);
        }

        participantRepository.findByTripAndMember_Id(trip, targetId)
                .ifPresentOrElse(participantRepository::delete,
                        () -> {
                            throw new TripException(NOT_FOUND_PARTICIPANT);
                        });

        applicationEventPublisher.publishEvent(new TripKickOutEvent(targetId, tripId));
    }
}
