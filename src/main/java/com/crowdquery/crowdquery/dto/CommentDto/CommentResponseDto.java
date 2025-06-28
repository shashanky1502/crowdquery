package com.crowdquery.crowdquery.dto.CommentDto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private String id; // Obfuscated ID
    private String text;
    private String parentQuestionId;
    private String parentCommentId;
    private String authorAnonymousUsername;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isOwner; // Can current user edit/delete
}