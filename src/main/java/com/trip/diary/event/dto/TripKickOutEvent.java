package com.trip.diary.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TripKickOutEvent {
    private Long memberId;
    private Long tripId;
}
