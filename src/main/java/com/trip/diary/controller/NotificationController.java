package com.trip.diary.controller;

import com.trip.diary.dto.DeleteNotificationsForm;
import com.trip.diary.dto.NotificationDto;
import com.trip.diary.security.MemberPrincipal;
import com.trip.diary.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    private ResponseEntity<List<NotificationDto>> readNotifications(@AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(notificationService.readNotifications(principal.getMember()));
    }

    @PutMapping("/{notificationId}")
    private ResponseEntity<Void> checkNotification(@PathVariable Long notificationId,
                                                   @AuthenticationPrincipal MemberPrincipal principal) {
        notificationService.checkNotification(notificationId, principal.getMember());
        return ResponseEntity.ok().build();
    }

    @PutMapping
    private ResponseEntity<Void> checkUnreadNotifications(@AuthenticationPrincipal MemberPrincipal principal) {
        notificationService.checkUnreadNotifications(principal.getMember());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    private ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId,
                                                    @AuthenticationPrincipal MemberPrincipal principal) {
        notificationService.deleteNotification(notificationId, principal.getMember());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    private ResponseEntity<Void> deleteNotifications(@RequestBody DeleteNotificationsForm form,
                                                     @AuthenticationPrincipal MemberPrincipal principal) {
        notificationService.deleteNotifications(form.getNotificationIds(), principal.getMember());
        return ResponseEntity.ok().build();
    }
}
