package com.trip.diary.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({CustomException.class})
    public ResponseEntity<ExceptionResponse> handleCustomException(CustomException e) {
        log.warn("api exception : {}", e.getErrorCode());
        return ResponseEntity.badRequest().body(new ExceptionResponse(e.getErrorCode().name(), e.getMessage()));
    }

//    @ExceptionHandler({Exception.class})
//    public ResponseEntity<ExceptionResponse> handleCustomException(Exception e) {
//        log.warn("internal error : {}", e.getMessage());
//        return ResponseEntity.badRequest().body(new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR.name(), e.getMessage()));
//    }
}
