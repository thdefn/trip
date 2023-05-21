package com.trip.diary.controller;

import com.trip.diary.dto.LocationDetailDto;
import com.trip.diary.dto.LocationDto;
import com.trip.diary.security.MemberPrincipal;
import com.trip.diary.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/trips/{tripId}/locations")
    private ResponseEntity<List<LocationDto>> readLocations(@PathVariable Long tripId,
                                                            @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(locationService.readLocations(tripId, principal.getMember()));
    }

    @GetMapping("/trips/{tripId}/locations/posts")
    private ResponseEntity<List<LocationDetailDto>> readLocationDetails(@PathVariable Long tripId,
                                                                        @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(locationService.readLocationDetails(tripId, principal.getMember()));
    }
}
