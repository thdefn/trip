package com.trip.diary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.domain.model.Trip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class TripDto {
    private Long id;
    private String title;
    private String description;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ParticipantDto> participants;

    public static TripDto of(Trip trip) {
        return TripDto.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .description(trip.getDescription())
                .participants(trip.getParticipants().stream().map(
                        participant -> ParticipantDto.of(participant.getMember().getId(),
                                ParticipantType.ACCEPTED.equals(participant.getType()),
                                participant.getMember().getProfilePath()))
                        .collect(Collectors.toList()))
                .build();
    }
}
