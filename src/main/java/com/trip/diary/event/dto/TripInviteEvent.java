package com.trip.diary.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class TripInviteEvent {
    private Set<Long> participantsIds;
    private Long tripId;
}
