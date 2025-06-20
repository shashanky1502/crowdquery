package com.crowdquery.crowdquery.service;

import java.util.List;
import java.util.UUID;

import com.crowdquery.crowdquery.dto.ChannelDto.ChannelRequestDto;
import com.crowdquery.crowdquery.dto.ChannelDto.ChannelResponseDto;

public interface ChannelService {

    ChannelResponseDto createChannel(ChannelRequestDto request);

    ChannelResponseDto joinChannel(String channelCode);

    void leaveChannel(UUID channelId);

    List<ChannelResponseDto> listJoinedChannels();

    ChannelResponseDto getChannel(UUID channelId);

    ChannelResponseDto getChannelByCode(String channelCode);

    boolean isChannelMember(UUID channelId);

    boolean isChannelModerator(UUID channelId);
}