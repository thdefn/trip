package com.trip.diary.controller;

import com.trip.diary.dto.*;
import com.trip.diary.security.MemberPrincipal;
import com.trip.diary.service.MemberSearchService;
import com.trip.diary.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/trips")
public class TripController {
    private final TripService tripService;

    @PostMapping
    private ResponseEntity<CreateTripDto> createTrip(@Valid @RequestBody CreateTripForm form,
                                                     @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(tripService.create(form, principal.getMember()));
    }

    @PutMapping("/{tripId}")
    private ResponseEntity<TripDto> updateTrip(@PathVariable Long tripId,
                                               @Valid @RequestBody UpdateTripForm form,
                                               @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(tripService.updateTrip(tripId, form, principal.getMember()));
    }
}
