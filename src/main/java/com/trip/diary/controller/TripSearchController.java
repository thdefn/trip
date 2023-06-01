package com.trip.diary.controller;

import com.trip.diary.dto.TripDto;
import com.trip.diary.security.MemberPrincipal;
import com.trip.diary.service.TripSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/trips")
public class TripSearchController {
    private final TripSearchService tripSearchService;

    @GetMapping("/search")
    private ResponseEntity<Page<TripDto>> search(@RequestParam int page,
                                                 @RequestParam String keyword,
                                                 @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(tripSearchService.search(page, keyword, principal.getMember()));
    }

    @GetMapping("/locations/search")
    private ResponseEntity<Page<TripDto>> searchByLocation(@RequestParam int page,
                                                           @RequestParam String keyword,
                                                           @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(tripSearchService.searchByLocation(page, keyword, principal.getMember()));
    }
}
