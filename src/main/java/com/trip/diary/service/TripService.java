package com.trip.diary.service;

import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.dto.*;
import com.trip.diary.elasticsearch.repository.MemberSearchRepository;
import com.trip.diary.event.dto.TripInviteEvent;
import com.trip.diary.event.dto.TripKickOutEvent;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.MemberException;
import com.trip.diary.exception.TripException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.trip.diary.domain.type.ParticipantType.ACCEPTED;
import static com.trip.diary.domain.type.ParticipantType.PENDING;
import static com.trip.diary.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    private final MemberRepository memberRepository;

    private final ParticipantRepository participantRepository;

    private final MemberSearchRepository memberSearchRepository;

    private final ApplicationEventPublisher applicationEventPublisher;


    @Transactional
    public CreateTripDto create(CreateTripForm form, Member member) {
        Trip trip = tripRepository.save(
                Trip.builder()
                        .title(form.getTitle())
                        .description(form.getDescription())
                        .leader(member)
                        .isPrivate(form.isPrivate())
                        .build()
        );

        Set<Long> participantsIds = form.getParticipants();
        participantsIds.add(member.getId());
        applicationEventPublisher.publishEvent(new TripInviteEvent(participantsIds, trip.getId()));

        return CreateTripDto.of(trip, member.getId(),
                participantRepository.saveAll(getParticipants(participantsIds, trip)));
    }

    private List<Participant> getParticipants(Set<Long> participantsIds, Trip trip) {
        Long leaderId = trip.getLeader().getId();
        return memberRepository.findAllByIdIn(participantsIds)
                .stream()
                .map(member -> Participant.builder()
                        .trip(trip)
                        .type(Objects.equals(member.getId(), leaderId) ? ACCEPTED : PENDING)
                        .member(member)
                        .build()
                ).collect(Collectors.toList());
    }


    @Transactional
    public List<ParticipantDto> getTripParticipants(Long tripId, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        List<Participant> participants = trip.getParticipants();

        if (trip.isPrivate() && !isMemberTripParticipants(participants, member.getId())) {
            throw new TripException(ErrorCode.NOT_AUTHORITY_READ_TRIP);
        }

        return participants
                .stream()
                .map(participant ->
                        ParticipantDto.of(participant, member.getId()))
                .collect(Collectors.toList());
    }

    private boolean isMemberTripParticipants(List<Participant> participants, Long memberId) {
        return participants.stream().anyMatch(participant -> participant.getMember().getId().equals(memberId));
    }


    public TripDto updateTrip(Long tripId, UpdateTripForm form, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        if (!Objects.equals(trip.getLeader().getId(), member.getId())) {
            throw new TripException(NOT_AUTHORITY_WRITE_TRIP);
        }

        trip.update(form);

        return TripDto.of(tripRepository.save(trip));
    }

    public List<MemberDto> searchAddableMembers(String keyword, Member member) {
        return memberSearchRepository.findByNicknameContainsIgnoreCase(keyword).stream()
                .filter(memberDocument -> !Objects.equals(memberDocument.getId(), member.getId()))
                .map(MemberDto::of)
                .collect(Collectors.toList());
    }

    public List<MemberDto> searchAddableMembersInTrip(Long tripId, String keyword, Member member) {
        return memberSearchRepository.findByNicknameContainsIgnoreCase(keyword).stream()
                .filter(memberDocument -> !Objects.equals(memberDocument.getId(), member.getId()))
                .map(memberDocument -> MemberDto.of(memberDocument, tripId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void invite(Long tripId, Long targetId, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        Member target = memberRepository.findById(targetId)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        if (!isMemberTripParticipants(trip.getParticipants(), member.getId())) {
            throw new TripException(NOT_AUTHORITY_WRITE_TRIP);
        }

        applicationEventPublisher.publishEvent(new TripInviteEvent(Set.of(targetId), trip.getId()));

        if (participantRepository.existsByTripAndMember(trip, target)) {
            throw new TripException(USER_ALREADY_INVITED);
        }

        participantRepository.save(Participant.builder()
                .member(target)
                .trip(trip)
                .type(PENDING)
                .build());
    }

    @Transactional
    public void kickOut(Long tripId, Long targetId, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        if (!Objects.equals(trip.getLeader().getId(), member.getId())) {
            throw new TripException(NOT_AUTHORITY_WRITE_TRIP);
        }

        applicationEventPublisher.publishEvent(new TripKickOutEvent(targetId, tripId));

        participantRepository.findByTripAndMember_Id(trip, targetId)
                .ifPresentOrElse(participantRepository::delete,
                        () -> {
                            throw new TripException(NOT_FOUND_PARTICIPANT);
                        });
    }
}
