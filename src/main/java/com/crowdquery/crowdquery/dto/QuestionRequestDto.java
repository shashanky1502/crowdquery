package com.crowdquery.crowdquery.dto;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionRequestDto {
    @NotBlank
    private String text;
    
    @NotNull
    private UUID channelId;
    
    private List<String> imageUrls;
}
