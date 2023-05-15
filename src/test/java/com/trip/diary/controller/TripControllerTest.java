package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.*;
import com.trip.diary.mockuser.WithMockCustomUser;
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
                        .memberProfileUrls(List.of("basic.jpg", "basic.jpg"))
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

    @Test
    @WithMockCustomUser
    @DisplayName("여행 참여 멤버 조회 성공")
    void getTripParticipantsTest_success() throws Exception {
        //given
        given(tripService.getTripParticipants(anyLong(), any()))
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
    @DisplayName("여행 기록장에 초대할 멤버 검색 성공")
    void searchAddableMembersTest_success() throws Exception {
        //given
        given(tripService.searchAddableMembers(anyString(), any()))
                .willReturn(List.of(
                        MemberDto.builder()
                                .id(1L)
                                .nickname("바밤바")
                                .profileUrl("basic.jpg")
                                .build(),
                        MemberDto.builder()
                                .id(3L)
                                .nickname("투움바 파스타")
                                .profileUrl("basic.jpg")
                                .build(),
                        MemberDto.builder()
                                .id(1L)
                                .nickname("바나나")
                                .profileUrl("basic.jpg")
                                .build()));
        //when
        //then
        mockMvc.perform(get("/trips/members/search")
                        .param("keyword", "바")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("생성된 여행 기록장에 초대할 멤버 검색 성공")
    void searchAddableMembersInTripTest_success() throws Exception {
        //given
        given(tripService.searchAddableMembers(anyString(), any()))
                .willReturn(List.of(
                        MemberDto.builder()
                                .id(1L)
                                .nickname("바밤바")
                                .profileUrl("basic.jpg")
                                .isInvited(true)
                                .build(),
                        MemberDto.builder()
                                .id(3L)
                                .nickname("투움바 파스타")
                                .profileUrl("basic.jpg")
                                .isInvited(false)
                                .build(),
                        MemberDto.builder()
                                .id(1L)
                                .nickname("바나나")
                                .profileUrl("basic.jpg")
                                .isInvited(false)
                                .build()));
        //when
        //then
        mockMvc.perform(get("/trips/{tripId}/members/search", 1L)
                        .param("keyword", "바")
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

}