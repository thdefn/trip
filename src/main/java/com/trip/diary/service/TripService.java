package com.trip.diary.service;

import com.trip.diary.domain.constants.Constants;
import com.trip.diary.domain.model.Bookmark;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.BookmarkRepository;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.dto.CreateTripDto;
import com.trip.diary.dto.CreateTripForm;
import com.trip.diary.dto.TripDto;
import com.trip.diary.dto.UpdateTripForm;
import com.trip.diary.event.dto.TripInviteEvent;
import com.trip.diary.event.dto.TripKickOutEvent;
import com.trip.diary.exception.MemberException;
import com.trip.diary.exception.TripException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.trip.diary.domain.constants.Constants.*;
import static com.trip.diary.domain.constants.ParticipantType.ACCEPTED;
import static com.trip.diary.domain.constants.ParticipantType.PENDING;
import static com.trip.diary.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;
    private final BookmarkRepository bookmarkRepository;
    private final NotificationService notificationService;
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

        notificationService.notifyInvitation(trip.getTitle(), member.getNickname(), participantsIds);
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


    public TripDto updateTrip(Long tripId, UpdateTripForm form, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        if (!Objects.equals(trip.getLeader().getId(), member.getId())) {
            throw new TripException(NOT_AUTHORITY_WRITE_TRIP);
        }

        trip.update(form);

        return TripDto.of(tripRepository.save(trip));
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

    public void bookmark(Long tripId, Member member) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripException(NOT_FOUND_TRIP));

        bookmarkRepository.findByTripAndMember(trip, member)
                .ifPresentOrElse(bookmarkRepository::delete,
                        () -> bookmarkRepository.save(Bookmark.builder()
                                .trip(trip)
                                .member(member)
                                .build()));
    }

    @Transactional
    public Slice<TripDto> readBookmarks(int page, Member member) {
        Slice<Bookmark> bookmarkSlice = bookmarkRepository
                .findByMember(member, PageRequest.of(page, BOOKMARK_PAGE_SIZE));
        return new SliceImpl<>(bookmarkSlice.stream()
                .filter(bookmark -> isMemberHaveReadAuthority(bookmark.getTrip(), member))
                .map(bookmark -> TripDto.of(bookmark.getTrip()))
                .collect(Collectors.toList()), bookmarkSlice.getPageable(), bookmarkSlice.hasNext());
    }

    private boolean isMemberHaveReadAuthority(Trip trip, Member member) {
        return (!trip.isPrivate() || participantRepository.existsByTripAndMemberAndType(trip, member, ACCEPTED));
    }
}
