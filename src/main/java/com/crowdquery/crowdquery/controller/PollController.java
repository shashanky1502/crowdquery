package com.crowdquery.crowdquery.controller;

import com.crowdquery.crowdquery.dto.PaginatedResponseDto;
import com.crowdquery.crowdquery.dto.PollDto.*;
import com.crowdquery.crowdquery.service.PollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PostMapping
    public ResponseEntity<PollResponseDto> createPoll(@Valid @RequestBody PollRequestDto request) {
        return ResponseEntity.ok(pollService.createPoll(request));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<PollResponseDto>> getPollsByChannel(
            @RequestParam String channelCode,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(pollService.getPollsByChannel(channelCode, limit, offset, sortBy, sortDir));
    }

    @GetMapping("/{pollId}")
    public ResponseEntity<PollResponseDto> getPoll(@PathVariable String pollId) {
        return ResponseEntity.ok(pollService.getPollById(pollId));
    }

    @DeleteMapping("/{pollId}")
    public ResponseEntity<Void> deletePoll(@PathVariable String pollId) {
        pollService.deletePoll(pollId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<PollResponseDto> voteOnPoll(
            @PathVariable String pollId,
            @Valid @RequestBody PollVoteRequestDto request) {
        return ResponseEntity.ok(pollService.voteOnPoll(pollId, request));
    }

    @PutMapping("/{pollId}/vote")
    public ResponseEntity<PollResponseDto> changeVote(
            @PathVariable String pollId,
            @Valid @RequestBody PollVoteRequestDto request) {
        return ResponseEntity.ok(pollService.changeVote(pollId, request));
    }

    @DeleteMapping("/{pollId}/vote")
    public ResponseEntity<PollResponseDto> removeVote(@PathVariable String pollId) {
        return ResponseEntity.ok(pollService.removeVote(pollId));
    }
}