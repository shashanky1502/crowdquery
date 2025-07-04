package com.crowdquery.crowdquery.dto.PollDto;

import lombok.Data;
import com.crowdquery.crowdquery.enums.PollStatus;
import com.crowdquery.crowdquery.dto.ReactionDto.ReactionSummaryDto;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PollResponseDto {
    private String id;
    private String question;
    private String channelCode;
    private String authorAnonymousUsername;
    private PollStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isOwner;
    private boolean canVote;
    private int totalVotes;
    private String userVote; // ID of option user voted for
    private List<PollOptionResponseDto> options;
    private ReactionSummaryDto reactions;
}