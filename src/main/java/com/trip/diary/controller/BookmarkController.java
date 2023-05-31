package com.trip.diary.controller;

import com.trip.diary.dto.TripDto;
import com.trip.diary.security.MemberPrincipal;
import com.trip.diary.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PutMapping("/trips/{tripId}/bookmarks")
    private ResponseEntity<Void> bookmark(@PathVariable Long tripId,
                                          @AuthenticationPrincipal MemberPrincipal principal) {
        bookmarkService.bookmark(tripId, principal.getMember());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trips/bookmarks")
    private ResponseEntity<Slice<TripDto>> readBookmarks(@RequestParam int page,
                                                         @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(bookmarkService.readBookmarks(page, principal.getMember()));
    }
}
