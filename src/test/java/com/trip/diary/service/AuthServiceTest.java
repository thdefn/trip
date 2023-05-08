package com.trip.diary.service;

import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.repository.MemberRepository;
import com.trip.diary.dto.SignUpForm;
import com.trip.diary.exception.CustomException;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.security.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
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
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

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
        CustomException exception = assertThrows(CustomException.class,
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
        CustomException exception = assertThrows(CustomException.class,
                () -> authService.register(form));
        //then
        assertEquals(exception.getErrorCode(), ErrorCode.MOBILE_ALREADY_REGISTERED);
    }

}