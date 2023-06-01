package com.trip.diary.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationDeleteEvent {
    private Long tripId;
    private String locationName;
}
