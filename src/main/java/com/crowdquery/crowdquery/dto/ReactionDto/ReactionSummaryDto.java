package com.crowdquery.crowdquery.dto.ReactionDto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ReactionSummaryDto {
    private Map<String, EmojiReactionDto> reactions; // Grouped by emoji
    private String userReaction; // Current user's reaction emoji (null if no reaction)

    @Data
    public static class EmojiReactionDto {
        private int count;
        private List<UserReactionDto> users;
    }

    @Data
    public static class UserReactionDto {
        private String userAnonymousUsername;
        private LocalDateTime reactedAt;
        private boolean isCurrentUser;
    }
}