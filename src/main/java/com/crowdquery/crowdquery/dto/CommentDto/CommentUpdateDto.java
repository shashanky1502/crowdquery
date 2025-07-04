package com.crowdquery.crowdquery.dto.CommentDto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CommentUpdateDto {
    @NotBlank(message = "Comment text cannot be empty")
    private String text;
}