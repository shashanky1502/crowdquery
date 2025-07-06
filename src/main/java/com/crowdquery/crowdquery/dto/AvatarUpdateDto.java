package com.crowdquery.crowdquery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AvatarUpdateDto {

    @NotBlank(message = "Avatar URL is required")
    @Pattern(regexp = "^https?://.*", message = "Avatar URL must be a valid HTTP/HTTPS URL")
    private String avatarUrl;
}