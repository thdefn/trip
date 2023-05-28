package com.trip.diary.service;

import com.trip.diary.domain.constants.NotificationType;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Notification;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.domain.repository.NotificationRepository;
import com.trip.diary.dto.NotificationDto;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.NotificationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private NotificationService notificationService;

    Member member = Member.builder()
            .id(1L)
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .profilePath(null)
            .phone("01011111111")
            .build();

    Member participant1 = Member.builder()
            .id(2L)
            .username("abcde")
            .nickname("하이")
            .profilePath(null)
            .build();

    @Test
    @DisplayName("알림 조회 성공")
    void readNotificationsTest_success() {
        //given
        given(notificationRepository.findByMember(any()))
                .willReturn(List.of(
                        Notification.builder()
                                .id(1L)
                                .message("[제주도 여행이 아니라 강릉 여행? ] [배짱부름... ] 댓글에 새벽임님이 댓글을 달았어요.")
                                .type(NotificationType.COMMENT)
                                .redirectPath("/trips/posts/7/comments")
                                .createdAt(LocalDateTime.now())
                                .readAt(null)
                                .member(member)
                                .build(),
                        Notification.builder()
                                .id(2L)
                                .message("[제주도 여행이 아니라 강릉 여행? ] 새벽임님이 기록 공간에 초대했어요. 여기를 눌러 초대에 응답하세요.")
                                .type(NotificationType.INVITE)
                                .redirectPath("/trips/invitations")
                                .createdAt(LocalDateTime.now())
                                .readAt(LocalDateTime.now())
                                .member(member)
                                .build()
                ));
        //when
        List<NotificationDto> result = notificationService.readNotifications(member);
        //then
        assertEquals(1L, result.get(0).getId());
        assertFalse(result.get(0).getIsRead());
        assertEquals(2L, result.get(1).getId());
        assertTrue(result.get(1).getIsRead());
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void checkNotificationTest_success() {
        //given
        Notification notification = mock(Notification.class);
        given(notificationRepository.findByIdAndMemberAndReadAtIsNull(anyLong(), any()))
                .willReturn(Optional.of(notification));
        //when
        notificationService.checkNotification(1L, member);
        //then
        verify(notification, times(1)).setReadAt();
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - 해당 알림 없음")
    void checkNotificationTest_failWhenNotFoundNotification() {
        //given
        given(notificationRepository.findByIdAndMemberAndReadAtIsNull(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        NotificationException exception = assertThrows(NotificationException.class,
                () -> notificationService.checkNotification(1L, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_NOTIFICATION, exception.getErrorCode());
    }

    @Test
    @DisplayName("안읽은 알림 전체 읽음 처리 성공")
    void checkUnreadNotificationsTest_success() {
        //given
        Notification notification = mock(Notification.class);
        given(notificationRepository.findByMemberAndReadAtIsNull(any()))
                .willReturn(List.of(notification, notification));
        //when
        notificationService.checkUnreadNotifications(member);
        //then
        verify(notification, times(2)).setReadAt();
    }

    @Test
    @DisplayName("알림 삭제 성공")
    void deleteNotificationTest_success() {
        //given
        given(notificationRepository.findByIdAndMember(anyLong(), any()))
                .willReturn(Optional.of(Notification.builder()
                        .id(1L)
                        .message("[제주도 여행이 아니라 강릉 여행? ] [배짱부름... ] 댓글에 새벽임님이 댓글을 달았어요.")
                        .type(NotificationType.COMMENT)
                        .redirectPath("/trips/posts/7/comments")
                        .createdAt(LocalDateTime.now())
                        .readAt(null)
                        .member(member)
                        .build()));
        //when
        notificationService.deleteNotification(anyLong(), any());
        //then
        verify(notificationRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("알림 삭제 실패 - 해당 알림 없음")
    void deleteNotificationTest_failWhenNotFoundNotification() {
        //given
        given(notificationRepository.findByIdAndMember(anyLong(), any()))
                .willReturn(Optional.empty());
        //when
        NotificationException exception = assertThrows(NotificationException.class,
                () -> notificationService.deleteNotification(1L, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_NOTIFICATION, exception.getErrorCode());
    }

    @Test
    @DisplayName("알림 전체 삭제 성공")
    void deleteNotificationsTest_success() {
        //given
        List<Notification> notifications = List.of(
                Notification.builder()
                        .id(1L)
                        .message("[제주도 여행이 아니라 강릉 여행? ] [배짱부름... ] 댓글에 새벽임님이 댓글을 달았어요.")
                        .type(NotificationType.COMMENT)
                        .redirectPath("/trips/posts/7/comments")
                        .createdAt(LocalDateTime.now())
                        .readAt(null)
                        .member(member)
                        .build(),
                Notification.builder()
                        .id(2L)
                        .message("[제주도 여행이 아니라 강릉 여행? ] 새벽임님이 기록 공간에 초대했어요. 여기를 눌러 초대에 응답하세요.")
                        .type(NotificationType.INVITE)
                        .redirectPath("/trips/invitations")
                        .createdAt(LocalDateTime.now())
                        .readAt(LocalDateTime.now())
                        .member(member)
                        .build()
        );
        given(notificationRepository.findByIdIn(anySet()))
                .willReturn(notifications);
        //when
        notificationService.deleteNotifications(Set.of(1L, 2L), member);
        //then
        verify(notificationRepository, times(1)).deleteAllInBatch(notifications);
    }

    @Test
    @DisplayName("알림 전체 삭제 실패 - 삭제할 유저의 알림 없음")
    void deleteNotificationsTest_failWhenNotFoundNotification() {
        //given
        given(notificationRepository.findByIdIn(anySet()))
                .willReturn(List.of(
                        Notification.builder()
                                .id(1L)
                                .message("[제주도 여행이 아니라 강릉 여행? ] [배짱부름... ] 댓글에 새벽임님이 댓글을 달았어요.")
                                .type(NotificationType.COMMENT)
                                .redirectPath("/trips/posts/7/comments")
                                .createdAt(LocalDateTime.now())
                                .readAt(null)
                                .member(member)
                                .build(),
                        Notification.builder()
                                .id(2L)
                                .message("[제주도 여행이 아니라 강릉 여행? ] 새벽임님이 기록 공간에 초대했어요. 여기를 눌러 초대에 응답하세요.")
                                .type(NotificationType.INVITE)
                                .redirectPath("/trips/invitations")
                                .createdAt(LocalDateTime.now())
                                .readAt(LocalDateTime.now())
                                .member(participant1)
                                .build()
                ));
        //when
        NotificationException exception = assertThrows(NotificationException.class,
                () -> notificationService.deleteNotifications(Set.of(1L, 2L), member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_NOTIFICATION, exception.getErrorCode());
    }

    @Test
    @DisplayName("초대 알림 생성 성공")
    void notifyInvitationTest_success() {
        //given
        given(memberRepository.findAllByIdIn(anySet()))
                .willReturn(List.of(member, participant1));
        //when
        notificationService.notifyInvitation("제주도 여행이 아니라 강릉 여행? ",
                "새벽임", Set.of(1L, 2L));
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        //then
        verify(notificationRepository, times(1)).saveAll(captor.capture());
        assertEquals("[제주도 여행이 아니라 강릉 여행? ] 새벽임님이 기록 공간에 초대했어요. 여기를 눌러 초대에 응답하세요.", captor.getValue().get(0).getMessage());
        assertEquals(1L, captor.getValue().get(0).getMember().getId());
        assertEquals(NotificationType.INVITE, captor.getValue().get(0).getType());
    }

    @Test
    @DisplayName("댓글 알림 생성 성공")
    void notifyCommentTest_success() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        //when
        notificationService.notifyComment("제주도 여행이 아니라 강릉 여행?", "제주공항", 12L,
                "새벽임", 1L);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        //then
        verify(notificationRepository, times(1)).save(captor.capture());
        assertEquals("[제주도 여행이 아니라 강릉 여행?] [제주공항] 사진에 새벽임님이 댓글을 달았어요.", captor.getValue().getMessage());
        assertEquals(NotificationType.COMMENT, captor.getValue().getType());
        assertEquals("/trips/posts/12/comments", captor.getValue().getRedirectPath());
    }

    @Test
    @DisplayName("대댓글 알림 생성 성공")
    void notifyReCommentTest_success() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        //when
        notificationService.notifyReComment("제주도 여행이 아니라 강릉 여행?", "123456789012345678901", 12L,
                "새벽임", 1L);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        //then
        verify(notificationRepository, times(1)).save(captor.capture());
        assertEquals("[제주도 여행이 아니라 강릉 여행?] [12345678901234567890...] 댓글에 새벽임님이 댓글을 달았어요.", captor.getValue().getMessage());
        assertEquals(NotificationType.RECOMMENT, captor.getValue().getType());
        assertEquals("/trips/posts/12/comments", captor.getValue().getRedirectPath());
    }

    @Test
    @DisplayName("대댓글 알림 생성 성공 - 댓글의 길이가 20 이하 일 때")
    void notifyReCommentTest_successCommentLengthIs20() {
        //given
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(member));
        //when
        notificationService.notifyReComment("제주도 여행이 아니라 강릉 여행?", "12345678901234567890", 12L,
                "새벽임", 1L);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        //then
        verify(notificationRepository, times(1)).save(captor.capture());
        assertEquals("[제주도 여행이 아니라 강릉 여행?] [12345678901234567890] 댓글에 새벽임님이 댓글을 달았어요.", captor.getValue().getMessage());
        assertEquals(NotificationType.RECOMMENT, captor.getValue().getType());
        assertEquals("/trips/posts/12/comments", captor.getValue().getRedirectPath());
    }

}