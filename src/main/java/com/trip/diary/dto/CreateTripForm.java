package com.trip.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTripForm {
    @NotBlank(message = "유효하지 않은 제목입니다.")
    @Size(min = 1, max = 20, message = "1자 이상 20자 이하의 제목을 입력해주세요.")
    private String title;

    @Size(max = 100, message = "5자 이상 100자 이하의 설명을 입력해주세요.")
    private String description;

    private boolean isPrivate;

    Set<Long> participants = new HashSet<>();
}
