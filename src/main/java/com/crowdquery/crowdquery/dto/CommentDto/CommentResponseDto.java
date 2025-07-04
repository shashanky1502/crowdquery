package com.crowdquery.crowdquery.dto.CommentDto;

import lombok.Data;
import java.time.LocalDateTime;

import com.crowdquery.crowdquery.dto.ReactionDto.ReactionSummaryDto;
import com.crowdquery.crowdquery.enums.CommentStatus;

@Data
public class CommentResponseDto {
    private String id; // Obfuscated ID
    private String text;
    private String parentQuestionId;
    private String parentCommentId;
    private String authorAnonymousUsername;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isOwner; // Can current user edit/delete
    private int commentLevel; // 0 = top-level, 1 = first reply, 2 = reply to reply, etc.
    private int replyCount = 0;
    private boolean isEdited;
    private ReactionSummaryDto reactions;
}