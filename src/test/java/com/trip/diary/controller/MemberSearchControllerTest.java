package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.MemberDto;
import com.trip.diary.mockuser.WithMockCustomUser;
import com.trip.diary.service.MemberSearchService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberSearchControllerTest {
    @MockBean
    private MemberSearchService memberSearchService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("여행 기록장에 초대할 멤버 검색 성공")
    void searchAddableMembersTest_success() throws Exception {
        //given
        given(memberSearchService.searchAddableMembers(anyString(), any()))
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
        given(memberSearchService.searchAddableMembers(anyString(), any()))
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

}