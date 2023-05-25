package com.trip.diary.exception;

import lombok.Getter;

@Getter
public class CommentException extends RuntimeException {
    private final ErrorCode errorCode;

    public CommentException(ErrorCode errorCode) {
        super(errorCode.getDetail());
        this.errorCode = errorCode;
    }
}
