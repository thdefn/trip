package com.trip.diary.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpForm {
    @Size(min = 5, max = 20, message = "아이디는 5자 이상 20자 이하입니다.")
    private String username;
    private String password;
    @Size(min = 11, max = 11, message = "유효한 핸드폰 번호인지 확인해주세요")
    private String phone;
    private String nickname;
}
