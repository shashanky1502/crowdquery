package com.crowdquery.crowdquery.service;

import org.springframework.security.access.prepost.PreAuthorize;

import com.crowdquery.crowdquery.dto.PaginatedResponseDto;
import com.crowdquery.crowdquery.dto.CommentDto.CommentRequestDto;
import com.crowdquery.crowdquery.dto.CommentDto.CommentResponseDto;
import com.crowdquery.crowdquery.dto.CommentDto.CommentUpdateDto;

public interface CommentService {
    CommentResponseDto createComment(CommentRequestDto request);

    PaginatedResponseDto<CommentResponseDto> getCommentsByQuestion(
            String questionId, int limit, int offset, String sortBy, String sortDir);

    PaginatedResponseDto<CommentResponseDto> getRepliesByComment(
            String commentId, int limit, int offset, String sortBy, String sortDir);

    @PreAuthorize("@commentServiceImpl.isCommentOwner(#commentId)")
    CommentResponseDto updateComment(String commentId, CommentUpdateDto request);

    @PreAuthorize("@commentServiceImpl.isCommentOwnerOrChannelModerator(#commentId)")
    void deleteComment(String commentId);

    boolean isCommentOwner(String commentId);

    boolean isCommentOwnerOrChannelModerator(String commentId);
}