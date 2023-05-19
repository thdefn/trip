package com.trip.diary.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostForm {
    @Size(max = 200, message = "200자 이하의 본문을 입력해주세요.")
    private String content;

    @Size(min = 1, max = 30, message = "장소명이 유효한지 확인해주세요.")
    private String location;
}
