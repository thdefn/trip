package com.trip.diary.dto;

import com.trip.diary.domain.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class NotificationDto {
    private Long id;
    private String message;
    private LocalDateTime createdAt;
    private String redirectPath;
    private Boolean isRead;

    public static NotificationDto of(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .redirectPath(notification.getRedirectPath())
                .isRead(Objects.nonNull(notification.getReadAt()))
                .build();
    }
}
