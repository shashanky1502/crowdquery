package com.crowdquery.crowdquery.dto.ChannelDto;

import java.util.UUID;

import lombok.Data;

@Data
public class ChannelResponseDto {
    private UUID id;
    private String name;
    private String channelCode;
    private String logoImageUrl;
    private boolean isReadOnly;
}