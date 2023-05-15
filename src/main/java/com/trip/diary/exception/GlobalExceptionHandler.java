package com.trip.diary.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({MemberException.class})
    public ResponseEntity<ExceptionResponse> handleMemberException(MemberException e) {
        log.warn("member exception : {}", e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(new ExceptionResponse(e.getErrorCode().name(), e.getMessage()));
    }

    @ExceptionHandler({TripException.class})
    public ResponseEntity<ExceptionResponse> handleTripException(TripException e) {
        log.warn("trip exception : {}", e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(new ExceptionResponse(e.getErrorCode().name(), e.getMessage()));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ExceptionResponse> handleException(Exception e) {
        log.warn("internal error : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), e.getMessage()));
    }
}
