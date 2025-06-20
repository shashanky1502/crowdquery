package com.crowdquery.crowdquery.dto;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.crowdquery.crowdquery.enums.QuestionStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionResponseDto {
    private UUID id;
    private String text;
    private UUID channelId;
    private UUID authorId;
    private String authorAnonymousUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private QuestionStatus status;
    private List<String> imageUrls;
}
