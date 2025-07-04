package com.crowdquery.crowdquery.dto.PollDto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PollVoteRequestDto {
    @NotBlank(message = "Poll option ID is required")
    private String pollOptionId; // Encoded
}