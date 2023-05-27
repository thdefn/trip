package com.trip.diary.exception;

import lombok.Getter;

@Getter
public class ParticipantException extends RuntimeException {
    private final ErrorCode errorCode;

    public ParticipantException(ErrorCode errorCode) {
        super(errorCode.getDetail());
        this.errorCode = errorCode;
    }
}
