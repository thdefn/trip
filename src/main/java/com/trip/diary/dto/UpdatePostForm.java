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
public class UpdatePostForm {
    @Size(max = 200, message = "200자 이하의 본문을 입력해주세요.")
    private String content;
}
