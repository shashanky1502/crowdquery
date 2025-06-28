package com.crowdquery.crowdquery.dto.CommentDto;

import lombok.Data;

@Data
public class CommentRequestDto {
    private String text;
    private String parentQuestionId; // Obfuscated question ID
    private String parentCommentId; // For nested comments, nullable
}
