package com.trip.diary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trip.diary.elasticsearch.model.MemberDocument;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isInvited;

    public static MemberDto of(MemberDocument memberDocument) {
        return MemberDto.builder()
                .id(memberDocument.getId())
                .nickname(memberDocument.getNickname())
                .profileUrl(memberDocument.getProfileUrl())
                .build();
    }

    public static MemberDto of(MemberDocument memberDocument, Long tripId) {
        return MemberDto.builder()
                .id(memberDocument.getId())
                .nickname(memberDocument.getNickname())
                .profileUrl(memberDocument.getProfileUrl())
                .isInvited(memberDocument.isInvitedInTrip(tripId))
                .build();
    }
}
