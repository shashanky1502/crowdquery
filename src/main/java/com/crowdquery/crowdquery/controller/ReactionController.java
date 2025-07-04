package com.crowdquery.crowdquery.controller;

import com.crowdquery.crowdquery.dto.ReactionDto.ReactionRequestDto;
import com.crowdquery.crowdquery.dto.ReactionDto.ReactionSummaryDto;
import com.crowdquery.crowdquery.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    // Add or update reaction
    @PostMapping
    public ResponseEntity<ReactionSummaryDto> addReaction(@Valid @RequestBody ReactionRequestDto request) {
        return ResponseEntity.ok(reactionService.addOrUpdateReaction(request));
    }

    // Remove reaction
    @DeleteMapping
    public ResponseEntity<ReactionSummaryDto> removeReaction(
            @RequestParam String targetId,
            @RequestParam String targetType) {
        return ResponseEntity.ok(reactionService.removeReaction(targetId, targetType));
    }

    // Get reactions for target
    @GetMapping
    public ResponseEntity<ReactionSummaryDto> getReactions(
            @RequestParam String targetId,
            @RequestParam String targetType) {
        return ResponseEntity.ok(reactionService.getReactionSummary(targetId, targetType));
    }
}