package com.crowdquery.crowdquery.dto.PollDto;

import lombok.Data;

@Data
public class PollOptionResponseDto {
    private String id;
    private String text;
    private Integer order;
    private int voteCount;
    private double percentage;
    private boolean userVoted;
}