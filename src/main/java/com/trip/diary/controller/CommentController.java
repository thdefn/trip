package com.trip.diary.controller;

import com.trip.diary.dto.CommentDto;
import com.trip.diary.dto.CreateCommentForm;
import com.trip.diary.security.MemberPrincipal;
import com.trip.diary.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/trips/posts/{postId}/comments")
    private ResponseEntity<CommentDto> createComment(@PathVariable Long postId,
                                                     @Valid @RequestBody CreateCommentForm form,
                                                     @AuthenticationPrincipal MemberPrincipal memberPrincipal) {
        return ResponseEntity.ok(commentService.create(postId, form, memberPrincipal.getMember()));
    }

    @GetMapping("/trips/posts/{postId}/comments")
    private ResponseEntity<List<CommentDto>> readComments(@PathVariable Long postId,

                                                          @AuthenticationPrincipal MemberPrincipal memberPrincipal) {
        return ResponseEntity.ok(commentService.read(postId, memberPrincipal.getMember()));
    }

    @PostMapping("/trips/posts/comments/{commentId}")
    private ResponseEntity<CommentDto> createReComment(@PathVariable Long commentId,
                                                       @Valid @RequestBody CreateCommentForm form,
                                                       @AuthenticationPrincipal MemberPrincipal memberPrincipal) {
        return ResponseEntity.ok(commentService.ReComment(commentId, form, memberPrincipal.getMember()));
    }

    @PutMapping("/trips/posts/comments/{commentId}")
    private ResponseEntity<CommentDto> updateComment(@PathVariable Long commentId,
                                                     @Valid @RequestBody CreateCommentForm form,
                                                     @AuthenticationPrincipal MemberPrincipal memberPrincipal) {
        return ResponseEntity.ok(commentService.update(commentId, form, memberPrincipal.getMember()));
    }

}
