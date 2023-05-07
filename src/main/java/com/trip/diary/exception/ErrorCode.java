package com.trip.diary.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST, "해당 유저를 찾을 수 없습니다."),
    PASSWORD_UNMATCHED(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    ID_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 존재하는 아이디입니다.");
    private final HttpStatus httpStatus;
    private final String detail;
}
