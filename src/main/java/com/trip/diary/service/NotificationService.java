package com.trip.diary.service;

import com.trip.diary.domain.constants.NotificationType;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Notification;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.domain.repository.NotificationRepository;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void notifyInvitation(String tripTitle, String senderNickname, Set<Long> targetMemberIds) {
        notificationRepository.saveAll(
                memberRepository.findAllByIdIn(targetMemberIds).stream()
                        .map(targetMember -> Notification.builder()
                                .message(getInvitationNoticeMessage(tripTitle, senderNickname))
                                .redirectPath("/trips/invitations")
                                .type(NotificationType.INVITE)
                                .member(targetMember)
                                .build()).collect(Collectors.toList())
        );
    }

    private String getInvitationNoticeMessage(String tripTitle, String senderNickname) {
        return "[" + tripTitle + "] " +
                senderNickname + "님이 기록 공간에 초대했어요. 여기를 눌러 초대에 응답하세요.";
    }

    @Transactional
    public void notifyComment(String tripTitle, String locationName, Long postId,
                              String senderNickname, Long receiverId) {
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));

        notificationRepository.save(
                Notification.builder()
                        .message(getCommentNoticeMessage(tripTitle, locationName, senderNickname))
                        .redirectPath("/trips/posts/" + postId + "/comments")
                        .type(NotificationType.COMMENT)
                        .member(receiver)
                        .build());
    }

    private String getCommentNoticeMessage(String tripTitle, String locationName, String senderNickname) {
        return "[" + tripTitle + "] " +
                "[" + locationName + "] 사진에" +
                senderNickname + "님이 댓글을 달았어요.";
    }

    @Transactional
    public void notifyReComment(String tripTitle, String commentContent, Long postId,
                              String senderNickname, Long receiverId) {
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));

        notificationRepository.save(
                Notification.builder()
                        .message(getReCommentNoticeMessage(tripTitle, commentContent, senderNickname))
                        .redirectPath("/trips/posts/" + postId + "/comments")
                        .type(NotificationType.RECOMMENT)
                        .member(receiver)
                        .build());
    }

    private String getReCommentNoticeMessage(String tripTitle, String commentContent, String senderNickname) {
        return "[" + tripTitle + "] " +
                "[" +
                ((commentContent.length() < 20) ? commentContent : commentContent.substring(0,20)) +
                "... ] 댓글에 " +
                senderNickname + "님이 댓글을 달았어요.";
    }
}
