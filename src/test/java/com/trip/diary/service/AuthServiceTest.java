package com.trip.diary.service;

import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.dto.SignInForm;
import com.trip.diary.dto.SignUpForm;
import com.trip.diary.dto.TokenDto;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.MemberException;
import com.trip.diary.security.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    Member member = Member.builder()
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .phone("01011111111")
            .build();

    @Test
    @DisplayName("회원가입 성공")
    void registerTest_success() {
        //given
        SignUpForm form = SignUpForm.builder()
                .username("qwerty99")
                .nickname("김맹맹")
                .password("1234567")
                .phone("01011111111")
                .build();
        given(memberRepository.existsByUsername(anyString())).willReturn(false);
        given(memberRepository.existsByPhone(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedpassword");
        given(memberRepository.save(any())).willReturn(member);
        //when
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        authService.register(form);
        //then
        verify(memberRepository, times(1)).save(captor.capture());
        assertEquals("qwerty99", captor.getValue().getUsername());
        assertEquals("01011111111", captor.getValue().getPhone());
        assertNotEquals("1234567", captor.getValue().getPassword());
    }

    @Test
    @DisplayName("회원가입 실패-이미 사용중인 아이디")
    void registerTest_failWhenIdAlreadyUsed() {
        //given
        SignUpForm form = SignUpForm.builder()
                .username("qwerty99")
                .nickname("김맹맹")
                .password("1234567")
                .phone("01011111111")
                .build();
        given(memberRepository.existsByUsername(anyString())).willReturn(true);
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> authService.register(form));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.ID_ALREADY_USED);
    }

    @Test
    @DisplayName("회원가입 실패-이미 등록된 핸드폰 번호")
    void registerTest_failWhenMobileAlreadyRegistered() {
        //given
        SignUpForm form = SignUpForm.builder()
                .username("qwerty99")
                .nickname("김맹맹")
                .password("1234567")
                .phone("01011111111")
                .build();
        given(memberRepository.existsByUsername(anyString())).willReturn(false);
        given(memberRepository.existsByPhone(anyString())).willReturn(true);
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> authService.register(form));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.MOBILE_ALREADY_REGISTERED);
    }

    @Test
    @DisplayName("로그인 성공")
    void authenticateTest_success() {
        //given
        SignInForm form = SignInForm.builder()
                .username("qwerty99")
                .password("1234567")
                .build();
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(any(), anyString())).willReturn(true);
        given(tokenProvider.generateToken(anyString())).willReturn("Bearer tokenstring");
        //when
        TokenDto tokenDto = authService.authenticate(form);
        //then
        assertEquals("Bearer tokenstring", tokenDto.getAccessToken());
    }

    @Test
    @DisplayName("로그인 실패 - 해당 아이디와 일치하는 유저 없음")
    void authenticateTest_failWhenNotFoundMember() {
        //given
        SignInForm form = SignInForm.builder()
                .username("qwerty99")
                .password("1234567")
                .build();
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.empty());
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> authService.authenticate(form));
        //then
        assertEquals(ErrorCode.NOT_FOUND_MEMBER, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 실패 - 패스워드가 다름")
    void authenticateTest_failWhenPasswordUnmatched() {
        //given
        SignInForm form = SignInForm.builder()
                .username("qwerty99")
                .password("1234567")
                .build();
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(any(), anyString())).willReturn(false);
        //when
        MemberException exception = assertThrows(MemberException.class,
                () -> authService.authenticate(form));
        //then
        assertEquals(ErrorCode.PASSWORD_UNMATCHED, exception.getErrorCode());
    }

}