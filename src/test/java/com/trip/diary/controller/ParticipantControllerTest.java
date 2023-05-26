package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.ParticipantDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

}