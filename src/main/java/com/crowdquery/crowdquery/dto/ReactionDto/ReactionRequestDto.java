package com.crowdquery.crowdquery.dto.ReactionDto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.crowdquery.crowdquery.enums.ReactionTargetType;

@Data
public class ReactionRequestDto {
    @NotBlank(message = "Target ID is required")
    private String targetId;

    @NotNull(message = "Target type is required")
    private ReactionTargetType targetType;

    @NotBlank(message = "Emoji is required")
    private String emoji;
}