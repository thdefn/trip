package com.trip.diary.exception;

import lombok.Getter;

@Getter
public class LocationException extends RuntimeException {
    private final ErrorCode errorCode;

    public LocationException(ErrorCode errorCode) {
        super(errorCode.getDetail());
        this.errorCode = errorCode;
    }
}
