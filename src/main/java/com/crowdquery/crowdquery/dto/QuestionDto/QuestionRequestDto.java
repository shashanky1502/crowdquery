package com.crowdquery.crowdquery.dto.QuestionDto;

import lombok.Data;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Data
public class QuestionRequestDto {
    private String text;
    private String channelCode;
    private List<MultipartFile> images;
}
