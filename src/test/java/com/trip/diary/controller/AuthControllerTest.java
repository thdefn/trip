package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.SignInForm;
import com.trip.diary.dto.SignUpForm;
import com.trip.diary.dto.TokenDto;
import com.trip.diary.security.TokenProvider;
import com.trip.diary.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @MockBean
    private AuthService authService;

    @MockBean
    TokenProvider tokenProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공")
    void registerTest_success() throws Exception {
        //given
        SignUpForm form = SignUpForm.builder()
                .username("qwerty99")
                .nickname("김맹맹")
                .password("1234567")
                .phone("01011111111")
                .build();
        //when
        //then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form))
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("로그인 성공")
    void authenticateTest_success() throws Exception {
        //given
        SignInForm form = SignInForm.builder()
                .username("qwerty99")
                .password("1234567")
                .build();
        given(authService.authenticate(any()))
                .willReturn(new TokenDto("jwttokenvalue"));
        //when
        //then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwttokenvalue"));
    }
}