package com.crowdquery.crowdquery.service;

import org.springframework.security.access.prepost.PreAuthorize;

import com.crowdquery.crowdquery.dto.PaginatedResponseDto;
import com.crowdquery.crowdquery.dto.QuestionDto.QuestionRequestDto;
import com.crowdquery.crowdquery.dto.QuestionDto.QuestionResponseDto;

public interface QuestionService {

    QuestionResponseDto createQuestionWithImages(QuestionRequestDto request);

    @PreAuthorize("@channelServiceImpl.isChannelMemberByCode(#channelCode)")
    PaginatedResponseDto<QuestionResponseDto> getQuestionsByChannel(
            String channelCode, int limit, int offset, String sortBy, String sortDir);

    QuestionResponseDto getQuestion(String questionId);

    @PreAuthorize("@questionServiceImpl.isQuestionOwner(#questionId)")
    QuestionResponseDto updateQuestion(String questionId, QuestionRequestDto request);

    @PreAuthorize("@questionServiceImpl.isQuestionOwnerOrChannelModerator(#questionId)")
    void deleteQuestion(String questionId);

    PaginatedResponseDto<QuestionResponseDto> getMyQuestions(
            int limit, int offset, String sortBy, String sortDir);

    PaginatedResponseDto<QuestionResponseDto> getQuestionsByUser(
            String username, int limit, int offset, String sortBy, String sortDir);

    boolean isQuestionOwner(String questionId);

    boolean isQuestionOwnerOrChannelModerator(String questionId);
}