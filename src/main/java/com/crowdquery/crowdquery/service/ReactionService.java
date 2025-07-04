package com.crowdquery.crowdquery.service;

import com.crowdquery.crowdquery.dto.ReactionDto.ReactionRequestDto;
import com.crowdquery.crowdquery.dto.ReactionDto.ReactionSummaryDto;

public interface ReactionService {

    // Add or update reaction (if user already reacted, update emoji)
    ReactionSummaryDto addOrUpdateReaction(ReactionRequestDto request);

    // Remove user's reaction
    ReactionSummaryDto removeReaction(String targetId, String targetType);

    // Get reaction summary for a target (with grouped emojis and user details)
    ReactionSummaryDto getReactionSummary(String targetId, String targetType);
}