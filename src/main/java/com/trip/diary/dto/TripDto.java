package com.trip.diary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.domain.model.Bookmark;
import com.trip.diary.domain.model.Location;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.elasticsearch.model.TripDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class TripDto {
    private Long id;
    private String title;
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isBookmarked;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ParticipantDto> participants;
    private Set<String> locations;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long countOfBookmark;

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
                .locations(trip.getLocations().stream()
                        .map(Location::getName).collect(Collectors.toSet()))
                .build();
    }

    public static TripDto of(Bookmark bookmark) {
        return TripDto.builder()
                .id(bookmark.getTrip().getId())
                .title(bookmark.getTrip().getTitle())
                .description(bookmark.getTrip().getDescription())
                .isBookmarked(true)
                .locations(bookmark.getTrip().getLocations().stream()
                        .map(Location::getName).collect(Collectors.toSet()))
                .build();
    }

    public static TripDto of(TripDocument document, boolean isBookmarked) {
        return TripDto.builder()
                .id(document.getId())
                .title(document.getTitle())
                .description(document.getDescription())
                .isBookmarked(isBookmarked)
                .locations(document.getLocations().stream()
                        .map(TripDocument.Location::getName)
                        .collect(Collectors.toSet()))
                .build();
    }

    public static TripDto of(TripBookmarkDto dto, List<String> locations, boolean isBookmarked) {
        return TripDto.builder()
                .id(dto.getTripId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .isBookmarked(isBookmarked)
                .countOfBookmark(dto.getCountOfBookmarked())
                .locations(Set.copyOf(locations))
                .build();
    }
}
