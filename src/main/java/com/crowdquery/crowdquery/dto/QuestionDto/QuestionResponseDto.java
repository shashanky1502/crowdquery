package com.crowdquery.crowdquery.dto.QuestionDto;

import com.crowdquery.crowdquery.dto.ReactionDto.ReactionSummaryDto;
import com.crowdquery.crowdquery.enums.QuestionStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionResponseDto {
    private String id; // Obfuscated/encoded ID, not raw UUID
    private String text;
    private String channelCode;
    private String channelName;
    private String authorAnonymousUsername;
    private QuestionStatus status;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long commentCount;
    private boolean isOwner; // Can current user edit/delete
    private ReactionSummaryDto reactions;
}
