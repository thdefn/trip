package com.trip.diary.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST, "해당 유저를 찾을 수 없습니다."),
    PASSWORD_UNMATCHED(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    MOBILE_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 존재하는 전화 번호입니다."),
    ID_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 존재하는 아이디입니다."),
    NOT_FOUND_TRIP(HttpStatus.BAD_REQUEST, "해당 여행 기록장을 찾을 수 없습니다."),
    USER_ALREADY_INVITED(HttpStatus.BAD_REQUEST, "이미 초대된 유저입니다."),
    NOT_FOUND_PARTICIPANT(HttpStatus.BAD_REQUEST, "해당 초대자를 찾을 수 없습니다."),
    NOT_AUTHORITY_READ_TRIP(HttpStatus.BAD_REQUEST, "해당 여행 기록장을 열람할 수 없습니다."),
    NOT_AUTHORITY_WRITE_TRIP(HttpStatus.BAD_REQUEST, "여행 기록장에 대한 해당 수정 권한이 없습니다."),
    NOT_FOUND_POST(HttpStatus.BAD_REQUEST, "해당 기록을 찾을 수 없습니다."),
    NOT_FOUND_LOCATION(HttpStatus.BAD_REQUEST, "해당 위치를 찾을 수 없습니다."),
    NOT_FOUND_COMMENT(HttpStatus.BAD_REQUEST, "해당 댓글을 찾을 수 없습니다."),
    NOT_COMMENT_OWNER(HttpStatus.BAD_REQUEST, "해당 댓글에 대한 작성자가 아닙니다."),
    NOT_POST_OWNER(HttpStatus.BAD_REQUEST, "해당 기록에 대한 작성자가 아닙니다."),
    NOT_INVITED_TRIP(HttpStatus.BAD_REQUEST, "초대받은 여행 기록장이 아닙니다."),
    CAN_NOT_RE_COMMENT_TO_RE_COMMENT(HttpStatus.BAD_REQUEST, "대댓글에 대댓글을 달 수 없습니다."),
    UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "업로드에 실패했습니다.")
    ;
    private final HttpStatus httpStatus;
    private final String detail;
}
