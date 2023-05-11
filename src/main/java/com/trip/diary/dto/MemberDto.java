package com.trip.diary.dto;

import com.trip.diary.domain.model.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class MemberDto {
    private Long id;
    private String nickname;
    private String profileUrl;

    public static MemberDto of(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileUrl(member.getProfileUrl())
                .build();
    }
}
