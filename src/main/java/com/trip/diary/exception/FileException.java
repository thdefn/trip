package com.trip.diary.exception;

import lombok.Getter;

@Getter
public class FileException extends RuntimeException {
    private final ErrorCode errorCode;

    public FileException(ErrorCode errorCode) {
        super(errorCode.getDetail());
        this.errorCode = errorCode;
    }
}
