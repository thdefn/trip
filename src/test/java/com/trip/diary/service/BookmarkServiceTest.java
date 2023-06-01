package com.trip.diary.service;

import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.domain.model.Bookmark;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.BookmarkRepository;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.TripRepository;
import com.trip.diary.dto.TripDto;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.TripException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {
    @Mock
    private TripRepository tripRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private BookmarkService bookmarkService;

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
            .build();

    @Test
    @DisplayName("북마크 성공 - 생성")
    void bookmarkTest_success() {
        //given
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(bookmarkRepository.findByTripAndMember(any(), any()))
                .willReturn(Optional.empty());
        //when
        bookmarkService.bookmark(1L, member);
        //then
        verify(bookmarkRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("북마크 성공 - 취소")
    void bookmarkTest_successCancel() {
        //given
        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(bookmarkRepository.findByTripAndMember(any(), any()))
                .willReturn(Optional.of(
                        Bookmark.builder()
                                .id(1L)
                                .member(member)
                                .trip(trip)
                                .build()
                ));
        //when
        bookmarkService.bookmark(1L, member);
        //then
        verify(bookmarkRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("북마크 생성 실패")
    void bookmarkTest_failWhenNotFoundTrip() {
        //given
        given(tripRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        TripException exception = assertThrows(TripException.class, () -> bookmarkService.bookmark(1L, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("북마크 조회 성공 - 유저가 북마크한 여행 기록장 읽기 권한 있을 때")
    void readBookmarksTest_successWhenUserHavePrivateTripAuthority() {
        //given
        Trip trip = Trip.builder()
                .id(1L)
                .title("첫번째 타이틀")
                .isPrivate(true)
                .description("첫번째 설명")
                .locations(new ArrayList<>())
                .participants(List.of(
                        Participant.builder()
                                .id(1L)
                                .member(member)
                                .build()
                ))
                .build();

        Trip trip2 = Trip.builder()
                .id(2L)
                .title("두번째 타이틀")
                .isPrivate(false)
                .description("두번째 설명")
                .locations(new ArrayList<>())
                .participants(List.of(
                        Participant.builder()
                                .id(1L)
                                .member(member)
                                .build()
                ))
                .build();

        given(bookmarkRepository.findByMember(any(), any()))
                .willReturn(new SliceImpl<>(
                        List.of(Bookmark.builder()
                                        .id(1L)
                                        .trip(trip)
                                        .member(member)
                                        .build(),
                                Bookmark.builder()
                                        .id(2L)
                                        .trip(trip2)
                                        .member(member)
                                        .build()
                        )));
        given(participantRepository.existsByTripAndMemberAndType(trip, member, ParticipantType.ACCEPTED))
                .willReturn(true);
        //when
        Slice<TripDto> result =  bookmarkService.readBookmarks(0, member);
        //then
        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals("첫번째 타이틀",result.getContent().get(0).getTitle());
        assertEquals("첫번째 설명",result.getContent().get(0).getDescription());
        assertEquals(2L, result.getContent().get(1).getId());
        assertEquals("두번째 타이틀",result.getContent().get(1).getTitle());
        assertEquals("두번째 설명",result.getContent().get(1).getDescription());
    }

    @Test
    @DisplayName("북마크 조회 성공 - 유저가 북마크한 여행 기록장 읽기 권한 없을 때")
    void readBookmarksTest_successWhenUserNotHavePrivateTripAuthority() {
        //given
        Trip trip = Trip.builder()
                .id(1L)
                .title("첫번째 타이틀")
                .isPrivate(true)
                .description("첫번째 설명")
                .locations(new ArrayList<>())
                .participants(List.of(
                        Participant.builder()
                                .id(1L)
                                .member(member)
                                .build()
                ))
                .build();

        Trip trip2 = Trip.builder()
                .id(2L)
                .title("두번째 타이틀")
                .isPrivate(false)
                .description("두번째 설명")
                .locations(new ArrayList<>())
                .participants(List.of(
                        Participant.builder()
                                .id(1L)
                                .member(member)
                                .build()
                ))
                .build();

        given(bookmarkRepository.findByMember(any(), any()))
                .willReturn(new SliceImpl<>(
                        List.of(Bookmark.builder()
                                        .id(1L)
                                        .trip(trip)
                                        .member(member)
                                        .build(),
                                Bookmark.builder()
                                        .id(2L)
                                        .trip(trip2)
                                        .member(member)
                                        .build()
                        )));
        given(participantRepository.existsByTripAndMemberAndType(trip, member, ParticipantType.ACCEPTED))
                .willReturn(false);
        //when
        Slice<TripDto> result =  bookmarkService.readBookmarks(0, member);
        //then
        assertEquals(1, result.getContent().size());
        assertEquals(2L, result.getContent().get(0).getId());
        assertEquals("두번째 타이틀",result.getContent().get(0).getTitle());
        assertEquals("두번째 설명",result.getContent().get(0).getDescription());
    }

}