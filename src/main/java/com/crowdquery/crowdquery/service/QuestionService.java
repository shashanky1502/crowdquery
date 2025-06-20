package com.crowdquery.crowdquery.service;
import com.crowdquery.crowdquery.dto.QuestionRequestDto;
import com.crowdquery.crowdquery.dto.QuestionResponseDto;

public interface QuestionService {
    QuestionResponseDto createQuestion(QuestionRequestDto req);
} 