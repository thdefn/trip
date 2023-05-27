package com.trip.diary.service;

import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.dto.ParticipantDto;
import com.trip.diary.dto.TripDto;
import com.trip.diary.event.dto.TripKickOutEvent;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.ParticipantException;
import com.trip.diary.exception.TripException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.trip.diary.exception.ErrorCode.NOT_FOUND_TRIP;
import static com.trip.diary.exception.ErrorCode.NOT_INVITED_TRIP;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final TripRepository tripRepository;

    private final ParticipantRepository participantRepository;

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
}
