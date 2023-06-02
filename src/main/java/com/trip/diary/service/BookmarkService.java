package com.trip.diary.service;

import com.trip.diary.domain.model.Bookmark;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.BookmarkRepository;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.dto.TripDto;
import com.trip.diary.exception.TripException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import static com.trip.diary.domain.constants.Constants.BOOKMARK_PAGE_SIZE;
import static com.trip.diary.domain.constants.ParticipantType.ACCEPTED;
import static com.trip.diary.exception.ErrorCode.NOT_FOUND_TRIP;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final TripRepository tripRepository;
    private final ParticipantRepository participantRepository;
    private final BookmarkRepository bookmarkRepository;

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
                .map(TripDto::of)
                .collect(Collectors.toList()), bookmarkSlice.getPageable(), bookmarkSlice.hasNext());
    }

    private boolean isMemberHaveReadAuthority(Trip trip, Member member) {
        return (!trip.isPrivate() || participantRepository.existsByTripAndMemberAndType(trip, member, ACCEPTED));
    }
}
