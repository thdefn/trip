package com.trip.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip.diary.dto.CreatePostForm;
import com.trip.diary.dto.PostDetailDto;
import com.trip.diary.dto.UpdatePostForm;
import com.trip.diary.mockuser.WithMockCustomUser;
import com.trip.diary.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {
    @MockBean
    private PostService postService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("여행 기록 생성 성공")
    void createPostTest_success() throws Exception {
        //given
        CreatePostForm form = CreatePostForm.builder()
                .content("여기는 제주공항")
                .location("제주공항")
                .build();
        MockMultipartFile json = new MockMultipartFile("form", "json",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(form).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile image = new MockMultipartFile("images", "image.jpg",
                MediaType.IMAGE_JPEG_VALUE, "abcde".getBytes());
        given(postService.create(anyLong(), any(), anyList(), any()))
                .willReturn(PostDetailDto.builder()
                        .id(2L)
                        .content("여기는 제주공항")
                        .imagePaths(List.of("/post/1234567543123.jpg"))
                        .locationId(2L)
                        .locationName("제주공항")
                        .authorId(1L)
                        .authorProfilePath("/profile/basic.jpg")
                        .authorNickname("강아지")
                        .build());
        //when
        //then
        mockMvc.perform(multipart("/trips/{tripId}/posts", 1L)
                        .file(image)
                        .file(json)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", TOKEN)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("여행 기록 수정 성공")
    void updatePostTest_success() throws Exception {
        //given
        UpdatePostForm form = UpdatePostForm.builder()
                .content("여기는 제주공항")
                .build();
        MockMultipartFile json = new MockMultipartFile("form", "json",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(form).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile image = new MockMultipartFile("images", "image.jpg",
                MediaType.IMAGE_JPEG_VALUE, "abcde".getBytes());
        given(postService.update(anyLong(), any(), anyList(), any()))
                .willReturn(PostDetailDto.builder()
                        .id(2L)
                        .content("여기는 제주공항")
                        .imagePaths(List.of("/post/1234567543123.jpg"))
                        .locationId(2L)
                        .locationName("제주공항")
                        .authorId(1L)
                        .authorProfilePath("/profile/basic.jpg")
                        .authorNickname("강아지")
                        .build());
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/trips/posts/{postId}", 1L)
                        .file(image)
                        .file(json)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", TOKEN)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("여행 기록 삭제 성공")
    void deletePostTest_success() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/trips/posts/{postId}", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}