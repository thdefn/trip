package com.trip.diary.controller;

import com.trip.diary.dto.ParticipantDto;
import com.trip.diary.dto.TripDto;
import com.trip.diary.security.MemberPrincipal;
import com.trip.diary.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @GetMapping("/trips/{tripId}/participants")
    private ResponseEntity<List<ParticipantDto>> getTripParticipants(@PathVariable Long tripId,
                                                                     @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(participantService.getTripParticipants(tripId, principal.getMember()));
    }

    @GetMapping("/trips/invitations")
    private ResponseEntity<List<TripDto>> getInvitedTripList(@AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(participantService.getInvitedTripList(principal.getMember()));
    }
}
