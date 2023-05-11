package com.trip.diary.controller;

import com.trip.diary.dto.SignInForm;
import com.trip.diary.dto.SignUpForm;
import com.trip.diary.dto.TokenDto;
import com.trip.diary.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    private ResponseEntity<Void> register(@Valid @RequestBody SignUpForm form) {
        authService.register(form);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signin")
    private ResponseEntity<TokenDto> authenticate(@Valid @RequestBody SignInForm form) {
        return ResponseEntity.ok(authService.authenticate(form));
    }

}
