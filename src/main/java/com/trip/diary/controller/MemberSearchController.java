package com.trip.diary.controller;

import com.trip.diary.dto.MemberDto;
import com.trip.diary.security.MemberPrincipal;
import com.trip.diary.service.MemberSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MemberSearchController {
    private final MemberSearchService memberSearchService;

    @GetMapping("/trips/members/search")
    private ResponseEntity<List<MemberDto>> searchAddableMembers(@RequestParam String keyword,
                                                                 @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(memberSearchService.searchAddableMembers(keyword, principal.getMember()));
    }

    @GetMapping("/trips/{tripId}/members/search")
    private ResponseEntity<List<MemberDto>> searchAddableMembersInTrip(@PathVariable Long tripId,
                                                                       @RequestParam String keyword,
                                                                       @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(memberSearchService.searchAddableMembersInTrip(tripId, keyword, principal.getMember()));
    }
}
