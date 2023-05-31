package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.*;
import com.trip.diary.mockuser.WithMockCustomUser;
import com.trip.diary.service.MemberSearchService;
import com.trip.diary.service.TripService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TripController.class)
@AutoConfigureMockMvc(addFilters = false)
class TripControllerTest {

    @MockBean
    private TripService tripService;

    @MockBean
    private MemberSearchService memberSearchService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("여행 기록장 생성 성공")
    void createTripTest_success() throws Exception {
        //given
        CreateTripForm form = CreateTripForm.builder()
                .title("제주도 여행팟")
                .description("제주도 여행을 떠나요")
                .isPrivate(true)
                .participants(new HashSet<>(Arrays.asList(2L, 3L)))
                .build();
        given(tripService.create(any(), any()))
                .willReturn(CreateTripDto.builder()
                        .id(1L)
                        .title("제주도 여행팟")
                        .description("제주도 여행을 떠나요")
                        .participants(
                                List.of(ParticipantDto.builder()
                                        .isAccepted(true)
                                        .isReader(true)
                                        .nickname("하이")
                                        .profileUrl(null)
                                        .id(2L)
                                        .build())
                        )
                        .build()
                );
        //when
        //then
        mockMvc.perform(post("/trips")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form))

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("여행 기록장 수정 성공")
    void updateTripTest_success() throws Exception {
        //given
        UpdateTripForm form = UpdateTripForm.builder()
                .title("제주도 여행팟")
                .description("제주도 여행을 떠나요")
                .isPrivate(true)
                .build();
        given(tripService.updateTrip(anyLong(), any(), any()))
                .willReturn(TripDto.builder()
                        .id(1L)
                        .title("임의의 타이틀")
                        .description("임의의 설명")
                        .participants(List.of(
                                ParticipantDto.builder()
                                        .id(1L)
                                        .profileUrl("profile/basic.jpg")
                                        .isAccepted(false)
                                        .build()
                        ))
                        .build()
                );
        //when
        //then
        mockMvc.perform(put("/trips/{tripId}", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form))

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}