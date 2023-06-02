package com.trip.diary.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TripBookmarkDto {
    private Long tripId;
    private boolean isPrivate;
    private String title;
    private String description;
    private Long countOfBookmarked;

    @QueryProjection
    public TripBookmarkDto(Long tripId, boolean isPrivate, String title, String description, Long countOfBookmarked) {
        this.tripId = tripId;
        this.isPrivate = isPrivate;
        this.title = title;
        this.description = description;
        this.countOfBookmarked = countOfBookmarked;
    }
}
