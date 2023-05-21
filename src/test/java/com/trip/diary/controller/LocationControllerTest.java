package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.LocationDetailDto;
import com.trip.diary.dto.LocationDto;
import com.trip.diary.mockuser.WithMockCustomUser;
import com.trip.diary.service.LocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LocationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocationControllerTest {
    @MockBean
    private LocationService locationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "Bearer token";

    @Test
    @DisplayName("여행 기록장 내 로케이션 리스트 조회 성공")
    @WithMockCustomUser
    void readLocationsTest_success() throws Exception {
        //given
        given(locationService.readLocations(anyLong(), any()))
                .willReturn(List.of(
                        LocationDto.builder()
                                .id(1L)
                                .name("제주공항")
                                .thumbnailPath("/posts/1234.jpg")
                                .numbersOfImages(3)
                                .build()
                ));
        //when
        //then
        mockMvc.perform(get("/trips/{tripId}/locations", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("여행 기록장 내 로케이션 리스트 상세 조회 성공")
    @WithMockCustomUser
    void readLocationDetailsTest_success() throws Exception {
        //given
        given(locationService.readLocationDetails(anyLong(), any()))
                .willReturn(List.of(
                        LocationDetailDto.builder()
                                .id(1L)
                                .name("제주공항")
                                .profilePaths(Set.of("/profile/123.jpg"))
                                .posts(List.of(
                                        new LocationDetailDto.PostDto(1L, "/posts/1.jpg"),
                                        new LocationDetailDto.PostDto(1L, "/posts/2.jpg"),
                                        new LocationDetailDto.PostDto(2L, "/posts/3.jpg")
                                ))
                                .build()
                ));
        //when
        //then
        mockMvc.perform(get("/trips/{tripId}/locations/posts", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}