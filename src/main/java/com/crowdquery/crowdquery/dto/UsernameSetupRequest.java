package com.crowdquery.crowdquery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsernameSetupRequest {
    @NotBlank(message = "Anonymous username is required")
    private String anonymousUsername;
}