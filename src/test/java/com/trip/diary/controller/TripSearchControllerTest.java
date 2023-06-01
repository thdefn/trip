package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.TripDto;
import com.trip.diary.mockuser.WithMockCustomUser;
import com.trip.diary.service.TripSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TripSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
class TripSearchControllerTest {
    @MockBean
    private TripSearchService tripSearchService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("여행 기록장 검색 성공")
    void searchTest_success() throws Exception {
        //given
        given(tripSearchService.search(anyInt(), anyString(), any()))
                .willReturn(new PageImpl<>(
                        List.of(
                                TripDto.builder()
                                        .id(1L)
                                        .title("제주도가 최고")
                                        .description("제주도 한달살기")
                                        .locations(Set.of("한라산국립공원", "성산일출봉"))
                                        .isBookmarked(true)
                                        .build()
                        )
                ));
        //when
        //then
        mockMvc.perform(get("/trips/search")
                        .param("keyword", "제")
                        .param("page", "0")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());


    }

    @Test
    @WithMockCustomUser
    @DisplayName("여행 기록장 로케이션 이름으로 검색 성공")
    void searchByLocationTest_success() throws Exception {
        //given
        given(tripSearchService.searchByLocation(anyInt(), anyString(), any()))
                .willReturn(new PageImpl<>(
                        List.of(
                                TripDto.builder()
                                        .id(1L)
                                        .title("제주도가 최고")
                                        .description("제주도 한달살기")
                                        .locations(Set.of("한라산국립공원", "성산일출봉"))
                                        .isBookmarked(true)
                                        .build()
                        )
                ));
        //when
        //then
        mockMvc.perform(get("/trips/locations/search")
                        .param("keyword", "한라산 국립공원")
                        .param("page", "0")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());


    }

}