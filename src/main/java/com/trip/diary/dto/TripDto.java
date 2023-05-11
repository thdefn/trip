package com.trip.diary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trip.diary.domain.model.Trip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class TripDto {
    private Long id;
    private String title;
    private String description;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> memberProfileUrls;

    public static TripDto of(Trip trip){
        return TripDto.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .description(trip.getDescription())
                .build();
    }
}
