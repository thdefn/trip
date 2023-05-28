package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.DeleteNotificationsForm;
import com.trip.diary.dto.NotificationDto;
import com.trip.diary.mockuser.WithMockCustomUser;
import com.trip.diary.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {
    @MockBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("알림 조회 성공")
    void readNotificationsTest_success() throws Exception {
        //given
        given(notificationService.readNotifications(any()))
                .willReturn(List.of(NotificationDto.builder()
                                .id(1L)
                                .createdAt(LocalDateTime.now())
                                .message("[제주도 여행이 아니라 강릉 여행?] [배짱부름] 댓글에 새벽임님이 댓글을 달았어요.")
                                .redirectPath("/trips/posts/7/comments")
                                .isRead(true)
                                .build(),
                        NotificationDto.builder()
                                .id(2L)
                                .createdAt(LocalDateTime.now())
                                .message("[제주도 여행이 아니라 강릉 여행?] [배짱부름] 댓글에 새벽임님이 댓글을 달았어요.")
                                .redirectPath("/trips/posts/7/comments")
                                .isRead(false)
                                .build()
                ));
        //when
        //then
        mockMvc.perform(get("/notifications")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("알림 읽음 처리 성공")
    void checkNotificationTest_success() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(put("/notifications/{notificationId}", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("모든 알림 읽음 처리 성공")
    void checkUnreadNotificationsTest_success() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(put("/notifications")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("알림 삭제 처리 성공")
    void deleteNotificationTest_success() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/notifications/{notificationId}", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("여러개 알림 삭제 처리 성공")
    void deleteNotificationsTest_success() throws Exception {
        //given
        DeleteNotificationsForm form = DeleteNotificationsForm.builder()
                .notificationIds(Set.of(1L, 2L))
                .build();
        //when
        //then
        mockMvc.perform(delete("/notifications")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}