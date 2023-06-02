package com.trip.diary.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationCreateEvent {
    private Long tripId;
    private String locationName;
}
