package com.trip.diary.event.dto;

import com.trip.diary.domain.model.Trip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripCreateEvent {
    private Trip trip;
    private Set<Long> participantsIds;
}
