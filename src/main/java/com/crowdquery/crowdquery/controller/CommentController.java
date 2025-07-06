package com.crowdquery.crowdquery.controller;

import com.crowdquery.crowdquery.dto.CommentDto.CommentRequestDto;
import com.crowdquery.crowdquery.dto.CommentDto.CommentResponseDto;
import com.crowdquery.crowdquery.dto.CommentDto.CommentUpdateDto;
import com.crowdquery.crowdquery.dto.PaginatedResponseDto;
import com.crowdquery.crowdquery.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(@RequestBody CommentRequestDto request) {
        return ResponseEntity.ok(commentService.createComment(request));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<CommentResponseDto>> getComments(
            @RequestParam String questionId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(commentService.getCommentsByQuestion(questionId, limit, offset, sortBy, sortDir));
    }

    @GetMapping("/replies")
    public ResponseEntity<PaginatedResponseDto<CommentResponseDto>> getReplies(
            @RequestParam String commentId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(commentService.getRepliesByComment(commentId, limit, offset, sortBy, sortDir));
    }

    @GetMapping("/me")
    public ResponseEntity<PaginatedResponseDto<CommentResponseDto>> getMyComments(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(commentService.getMyComments(limit, offset, sortBy, sortDir));
    }

    // Get comments by specific user
    @GetMapping("/user/{username}")
    public ResponseEntity<PaginatedResponseDto<CommentResponseDto>> getCommentsByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(commentService.getCommentsByUser(username, limit, offset, sortBy, sortDir));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable String commentId,
            @RequestBody CommentUpdateDto request) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}