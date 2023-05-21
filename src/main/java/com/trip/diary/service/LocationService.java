package com.trip.diary.service;

import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.LocationRepository;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.domain.type.ParticipantType;
import com.trip.diary.dto.LocationDetailDto;
import com.trip.diary.dto.LocationDto;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.TripException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.trip.diary.domain.type.ParticipantType.ACCEPTED;
import static com.trip.diary.exception.ErrorCode.NOT_AUTHORITY_READ_TRIP;

@Service
@AllArgsConstructor
public class LocationService {
    private final TripRepository tripRepository;

    private final ParticipantRepository participantRepository;
    private final LocationRepository locationRepository;

    @Transactional
    public List<LocationDetailDto> readLocationDetails(Long tripId, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(ErrorCode.NOT_FOUND_TRIP));

        if (trip.isPrivate() && !isMemberAcceptedTripParticipants(trip, member)) {
            throw new TripException(NOT_AUTHORITY_READ_TRIP);
        }

        return locationRepository.findByTripOrderByIdDesc(trip)
                .stream().map(LocationDetailDto::of).collect(Collectors.toList());
    }

    @Transactional
    public List<LocationDto> readLocations(Long tripId, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(ErrorCode.NOT_FOUND_TRIP));

        if (trip.isPrivate() && !isMemberAcceptedTripParticipants(trip, member)) {
            throw new TripException(NOT_AUTHORITY_READ_TRIP);
        }

        return locationRepository.findByTripOrderByIdDesc(trip)
                .stream().map(LocationDto::of).collect(Collectors.toList());
    }

    private boolean isMemberAcceptedTripParticipants(Trip trip, Member member) {
        return participantRepository.existsByTripAndMemberAndType(trip, member, ACCEPTED);
    }
}
