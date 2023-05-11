package com.trip.diary.dto;

import com.trip.diary.domain.model.Participant;
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
public class CreateTripDto {
    private Long id;
    private String title;
    private String description;
    private List<ParticipantDto> participants;

    public static CreateTripDto of(Trip trip, Long readerId, List<Participant> participants){
        return CreateTripDto.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .description(trip.getDescription())
                .participants(participants.stream().map(participant ->
                                ParticipantDto.of(participant, readerId))
                                .collect(Collectors.toList()))
                .build();
    }
}
