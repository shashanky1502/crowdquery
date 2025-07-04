package com.crowdquery.crowdquery.dto.PollDto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class PollRequestDto {

    @NotBlank(message = "Poll question is required")
    @Size(max = 500, message = "Question cannot exceed 500 characters")
    private String question;

    @NotBlank(message = "Channel ID is required")
    private String channelCode; // Encoded

    @NotEmpty(message = "At least 2 options are required")
    @Size(min = 2, max = 10, message = "Poll must have between 2 and 10 options")
    private List<@NotBlank @Size(max = 200) String> options;

    private Integer expiryHours = 24;
}