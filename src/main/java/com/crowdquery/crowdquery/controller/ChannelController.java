package com.crowdquery.crowdquery.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.crowdquery.crowdquery.dto.ChannelDto.ChannelRequestDto;
import com.crowdquery.crowdquery.dto.ChannelDto.ChannelResponseDto;
import com.crowdquery.crowdquery.service.ChannelService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<ChannelResponseDto> createChannel(@RequestBody ChannelRequestDto request) {
        return ResponseEntity.ok(channelService.createChannel(request));
    }

    @PostMapping("/join")
    public ResponseEntity<ChannelResponseDto> joinChannel(@RequestParam String channelCode) {
        return ResponseEntity.ok(channelService.joinChannel(channelCode));
    }

    @PostMapping("/{channelId}/leave")
    public ResponseEntity<Void> leaveChannel(@PathVariable UUID channelId) {
        channelService.leaveChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/joined")
    public ResponseEntity<List<ChannelResponseDto>> listJoinedChannels() {
        return ResponseEntity.ok(channelService.listJoinedChannels());
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<ChannelResponseDto> getChannel(@PathVariable UUID channelId) {
        return ResponseEntity.ok(channelService.getChannel(channelId));
    }

    @GetMapping("/code/{channelCode}")
    public ResponseEntity<ChannelResponseDto> getChannelByCode(@PathVariable String channelCode) {
        return ResponseEntity.ok(channelService.getChannelByCode(channelCode));
    }
}