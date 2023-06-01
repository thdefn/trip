package com.trip.diary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.domain.model.Participant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ParticipantDto {
    private Long id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nickname;
    private String profileUrl;
    private Boolean isAccepted;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isReader;

    public static ParticipantDto of(Participant participant, Long readerId) {
        return ParticipantDto.builder()
                .id(participant.getMember().getId())
                .nickname(participant.getMember().isReader(readerId) ? participant.getMember().getNickname() + "(나)"
                        : participant.getMember().getNickname())
                .profileUrl(participant.getMember().getProfilePath())
                .isAccepted(participant.getType().equals(ParticipantType.ACCEPTED))
                .isReader(participant.getMember().isReader(readerId))
                .build();
    }

    public static ParticipantDto of(Long id, boolean isAccepted, String profileUrl) {
        return ParticipantDto.builder()
                .id(id)
                .isAccepted(isAccepted)
                .profileUrl(profileUrl)
                .build();
    }
}
