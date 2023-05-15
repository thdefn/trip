package com.trip.diary.exception;

import lombok.Getter;

@Getter
public class TripException extends RuntimeException {
    private final ErrorCode errorCode;

    public TripException(ErrorCode errorCode) {
        super(errorCode.getDetail());
        this.errorCode = errorCode;
    }
}
