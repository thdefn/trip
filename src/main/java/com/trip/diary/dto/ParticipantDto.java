package com.trip.diary.dto;

import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.type.ParticipantType;
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
    private String nickname;
    private String profileUrl;
    private boolean isAccepted;
    private boolean isReader;

    public static ParticipantDto of(Participant participant, Long readerId) {
        boolean isReader = readerId.equals(participant.getMember().getId());
        return ParticipantDto.builder()
                .id(participant.getMember().getId())
                .nickname(isReader ? participant.getMember().getNickname() + "(나)"
                        : participant.getMember().getNickname())
                .profileUrl(participant.getMember().getProfileUrl())
                .isAccepted(participant.getType().equals(ParticipantType.ACCEPTED))
                .isReader(isReader)
                .build();
    }
}
