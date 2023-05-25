package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.CommentDto;
import com.trip.diary.dto.CreateCommentForm;
import com.trip.diary.mockuser.WithMockCustomUser;
import com.trip.diary.service.CommentService;
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

@WebMvcTest(controllers = CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {
    @MockBean
    private CommentService commentService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "Bearer token";

    @Test
    @DisplayName("기록에 댓글 달기 성공")
    @WithMockCustomUser
    void createCommentTest_success() throws Exception {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("여기 진짜 좋았다").build();
        given(commentService.create(anyLong(), any(), any()))
                .willReturn(CommentDto.builder()
                        .id(1L)
                        .authorId(1L)
                        .authorNickname("하이")
                        .authorProfilePath("profile/basic.jpg")
                        .isReader(true)
                        .content("여기 진짜 좋았다")
                        .build()
                );
        //when
        //then
        mockMvc.perform(post("/trips/posts/{postId}/comments", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 조회 성공")
    @WithMockCustomUser
    void readCommentsTest_success() throws Exception {
        //given
        given(commentService.read(anyLong(), any()))
                .willReturn(List.of(
                                CommentDto.builder()
                                        .id(1L)
                                        .authorId(1L)
                                        .authorNickname("하이")
                                        .authorProfilePath("profile/basic.jpg")
                                        .isReader(true)
                                        .content("여기 진짜 좋았다")
                                        .reComments(List.of(
                                                CommentDto.builder()
                                                        .id(2L)
                                                        .authorId(2L)
                                                        .authorNickname("송송이")
                                                        .authorProfilePath("profile/basic.jpg")
                                                        .isReader(false)
                                                        .content("나두좋았어")
                                                        .build(),
                                                CommentDto.builder()
                                                        .id(3L)
                                                        .authorId(3L)
                                                        .authorNickname("toto")
                                                        .authorProfilePath("profile/basic.jpg")
                                                        .isReader(false)
                                                        .content("ㅇㅈㅇㅈ 너무예뻤어")
                                                        .build()
                                        ))
                                        .build(),
                                CommentDto.builder()
                                        .id(4L)
                                        .authorId(2L)
                                        .authorNickname("송송이")
                                        .authorProfilePath("profile/basic.jpg")
                                        .isReader(false)
                                        .content("이 순간 잊지 못해")
                                        .build()
                        )
                );
        //when
        //then
        mockMvc.perform(get("/trips/posts/{postId}/comments", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("대댓글 달기 성공")
    @WithMockCustomUser
    void createReCommentTest_success() throws Exception {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("여기 진짜 좋았다").build();
        given(commentService.reComment(anyLong(), any(), any()))
                .willReturn(CommentDto.builder()
                        .id(2L)
                        .authorId(1L)
                        .authorNickname("하이")
                        .authorProfilePath("profile/basic.jpg")
                        .isReader(true)
                        .content("인정인정")
                        .build()
                );
        //when
        //then
        mockMvc.perform(post("/trips/posts/comments/{commentId}", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    @WithMockCustomUser
    void updateCommentTest_success() throws Exception {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("여기 진짜 좋았다").build();
        given(commentService.update(anyLong(), any(), any()))
                .willReturn(CommentDto.builder()
                        .id(2L)
                        .authorId(1L)
                        .authorNickname("하이")
                        .authorProfilePath("profile/basic.jpg")
                        .isReader(true)
                        .content("인정인정")
                        .build()
                );
        //when
        //then
        mockMvc.perform(post("/trips/posts/comments/{commentId}", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 좋아요, 좋아요 취소 성공")
    @WithMockCustomUser
    void likeCommentTest_success() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(put("/trips/posts/comments/{commentId}/like", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}