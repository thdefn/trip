package com.trip.diary.controller;

import com.trip.diary.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/new")
public class NewController {

    @GetMapping
    private ResponseEntity<Void> register(@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
        log.error(memberPrincipal.getUsername());
        log.error(memberPrincipal.getId().toString());
        return ResponseEntity.ok().build();
    }

}
