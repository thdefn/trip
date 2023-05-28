package com.trip.diary.exception;

import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException {
    private final ErrorCode errorCode;

    public NotificationException(ErrorCode errorCode) {
        super(errorCode.getDetail());
        this.errorCode = errorCode;
    }
}
