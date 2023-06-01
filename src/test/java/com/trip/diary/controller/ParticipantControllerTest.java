package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.ParticipantDto;
import com.trip.diary.dto.TripDto;
import com.trip.diary.mockuser.WithMockCustomUser;
import com.trip.diary.service.ParticipantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ParticipantController.class)
@AutoConfigureMockMvc(addFilters = false)
class ParticipantControllerTest {
    @MockBean
    private ParticipantService participantService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("여행 참여 멤버 조회 성공")
    void getTripParticipantsTest_success() throws Exception {
        //given
        given(participantService.getTripParticipants(anyLong(), any()))
                .willReturn(
                        List.of(
                                ParticipantDto.builder()
                                        .id(1L)
                                        .isAccepted(true)
                                        .isReader(true)
                                        .nickname("안녕")
                                        .profileUrl("basic.jpg")
                                        .build(),
                                ParticipantDto.builder()
                                        .id(2L)
                                        .isAccepted(true)
                                        .isReader(false)
                                        .nickname("정희")
                                        .profileUrl("basic.jpg")
                                        .build()
                        )
                );
        //when
        //then
        mockMvc.perform(get("/trips/{tripId}/participants", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("초대 목록 조회 성공")
    void getInvitedTripListTest_success() throws Exception {
        //given
        given(participantService.getInvitedTripList(any()))
                .willReturn(List.of(
                        TripDto.builder()
                                .id(1L)
                                .title("제주도여행팟")
                                .description("4/21 제주도 여행을 떠난 사람들의 모임")
                                .participants(List.of(
                                        ParticipantDto.builder()
                                                .id(1L)
                                                .isAccepted(true)
                                                .profileUrl("profile/basic.jpg")
                                                .build(),
                                        ParticipantDto.builder()
                                                .id(2L)
                                                .isAccepted(false)
                                                .profileUrl("profile/basic.jpg")
                                                .build(),
                                        ParticipantDto.builder()
                                                .id(3L)
                                                .isAccepted(false)
                                                .profileUrl("profile/basic.jpg")
                                                .build()
                                ))
                                .build()
                ));
        //when
        //then
        mockMvc.perform(get("/trips/invitations")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("여행기록장 초대 수락 성공")
    void acceptTripInvitationTest_success() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(put("/trips/{tripId}/invitations", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("여행기록장 초대 거절 성공")
    void denyTripInvitationTest_success() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/trips/{tripId}/invitations", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("여행 기록장에 유저 초대 or 초대 취소 성공")
    void inviteOrCancelTest_success() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(put("/trips/{tripId}/members/{memberId}", 1L, 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("여행 기록장에 유저 강제 퇴장 성공")
    void kickOutTest_success() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/trips/{tripId}/members/{memberId}", 1L, 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}