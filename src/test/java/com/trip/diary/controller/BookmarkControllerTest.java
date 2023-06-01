package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.ParticipantDto;
import com.trip.diary.dto.TripDto;
import com.trip.diary.mockuser.WithMockCustomUser;
import com.trip.diary.service.BookmarkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookmarkController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookmarkControllerTest {
    @MockBean
    BookmarkService bookmarkService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("여행기록장 북마크 생성 성공")
    void bookmarkTest_success() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(put("/trips/{tripId}/bookmarks", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("북마크한 여행 기록장 조회 성공")
    void readBookmarksTest_success() throws Exception {
        //given
        given(bookmarkService.readBookmarks(anyInt(), any()))
                .willReturn(new SliceImpl<>(List.of(
                        TripDto.builder()
                                .id(1L)
                                .title("제주도여행")
                                .description("제주도최고최고")
                                .participants(List.of(
                                        ParticipantDto.builder()
                                                .id(1L)
                                                .profileUrl("profiles/basic.jpg")
                                                .isReader(true)
                                                .isAccepted(true)
                                                .nickname("냠냠")
                                                .build()
                                ))
                                .locations(List.of("제주공항", "김포공항", "제주올레길"))
                                .build()
                )));
        //when
        //then
        mockMvc.perform(get("/trips/bookmarks")
                        .queryParam("page","0")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}