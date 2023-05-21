package com.trip.diary.controller;

import com.trip.diary.dto.CreatePostForm;
import com.trip.diary.dto.PostDetailDto;
import com.trip.diary.dto.UpdatePostForm;
import com.trip.diary.security.MemberPrincipal;
import com.trip.diary.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/trips/{tripId}/posts")
    private ResponseEntity<PostDetailDto> createPost(@PathVariable Long tripId,
                                                     @RequestPart List<MultipartFile> images,
                                                     @RequestPart @Valid CreatePostForm form,
                                                     @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(postService.create(tripId, form, images, principal.getMember()));
    }

    @PutMapping("/trips/posts/{postId}")
    private ResponseEntity<PostDetailDto> updatePost(@PathVariable Long postId,
                                                     @RequestPart List<MultipartFile> images,
                                                     @RequestPart @Valid UpdatePostForm form,
                                                     @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(postService.update(postId, form, images, principal.getMember()));
    }

    @DeleteMapping("/trips/posts/{postId}")
    private ResponseEntity<Void> deletePost(@PathVariable Long postId,
                                            @AuthenticationPrincipal MemberPrincipal principal) {
        postService.delete(postId, principal.getMember());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/trips/posts/{postId}/like")
    private ResponseEntity<Void> likePost(@PathVariable Long postId,
                                          @AuthenticationPrincipal MemberPrincipal principal) {
        postService.like(postId, principal.getMember());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trips/{tripId}/locations/{locationId}")
    private ResponseEntity<List<PostDetailDto>> readPostsByLocation(@PathVariable Long tripId,
                                                                    @PathVariable Long locationId,
                                                                    @AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(
                postService.readPostsByLocation(tripId, locationId, principal.getMember()));
    }
}
