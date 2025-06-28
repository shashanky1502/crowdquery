package com.crowdquery.crowdquery.service.impl;

import com.crowdquery.crowdquery.dto.CommentDto.CommentRequestDto;
import com.crowdquery.crowdquery.dto.CommentDto.CommentResponseDto;
import com.crowdquery.crowdquery.dto.PaginatedResponseDto;
import com.crowdquery.crowdquery.model.*;
import com.crowdquery.crowdquery.repository.*;
import com.crowdquery.crowdquery.service.CommentService;
import com.crowdquery.crowdquery.util.IdEncoder;
import com.crowdquery.crowdquery.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ChannelMembershipRepository channelMembershipRepository;
    private final IdEncoder idEncoder;

    @Override
    @Transactional
    public CommentResponseDto createComment(CommentRequestDto request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        UUID parentQuestionId = idEncoder.decode(request.getParentQuestionId());
        Question parentQuestion = questionRepository.findById(parentQuestionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Question not found"));

        // Verify user is a channel member
        if (!channelMembershipRepository.existsByChannelAndUser(parentQuestion.getChannel(), author)) {
            throw new ResponseStatusException(FORBIDDEN, "You are not a member of this channel");
        }

        Comment.CommentBuilder commentBuilder = Comment.builder()
                .text(request.getText())
                .parentContentId(parentQuestionId)
                .author(author);

        // Handle nested comments
        if (request.getParentCommentId() != null) {
            UUID parentCommentId = idEncoder.decode(request.getParentCommentId());
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Parent comment not found"));
            commentBuilder.parentComment(parentComment);
        }

        Comment comment = commentBuilder.build();
        Comment saved = commentRepository.save(comment);
        return mapToResponseDto(saved);
    }

    @Override
    public PaginatedResponseDto<CommentResponseDto> getCommentsByQuestion(
            String questionId, int page, int size, String sortBy, String sortDir) {

        UUID parentQuestionId = idEncoder.decode(questionId);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Comment> commentsPage = commentRepository.findByParentContentIdAndIsDeletedFalse(
                parentQuestionId, pageable);

        return new PaginatedResponseDto<>(
                commentsPage.getContent().stream()
                        .map(this::mapToResponseDto)
                        .toList(),
                new PaginatedResponseDto.Pagination(
                        size,
                        page * size,
                        commentsPage.getTotalElements(),
                        commentsPage.hasNext()));
    }

    @Override
    @Transactional
    public CommentResponseDto updateComment(String commentId, CommentRequestDto request) {
        UUID realCommentId = idEncoder.decode(commentId);
        Comment comment = commentRepository.findById(realCommentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Comment not found"));

        comment.setText(request.getText());
        Comment saved = commentRepository.save(comment);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteComment(String commentId) {
        UUID realCommentId = idEncoder.decode(commentId);
        Comment comment = commentRepository.findById(realCommentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Comment not found"));

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    public boolean isCommentOwner(String commentId) {
        try {
            UUID realCommentId = idEncoder.decode(commentId);
            UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
            if (currentUserId == null)
                return false;

            Comment comment = commentRepository.findById(realCommentId).orElse(null);
            return comment != null && comment.getAuthor().getId().equals(currentUserId);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isCommentOwnerOrChannelModerator(String commentId) {
        try {
            UUID realCommentId = idEncoder.decode(commentId);
            UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
            if (currentUserId == null)
                return false;

            Comment comment = commentRepository.findById(realCommentId).orElse(null);
            if (comment == null)
                return false;

            // Check if owner
            if (comment.getAuthor().getId().equals(currentUserId)) {
                return true;
            }

            // Get the channel from the parent question
            Question parentQuestion = questionRepository.findById(comment.getParentContentId()).orElse(null);
            if (parentQuestion == null)
                return false;

            // Check if channel moderator
            return channelMembershipRepository.findByChannelIdAndUserId(parentQuestion.getChannel().getId(), currentUserId)
                    .map(membership -> membership.getRole() == com.crowdquery.crowdquery.enums.ChannelRole.MODERATOR)
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    private CommentResponseDto mapToResponseDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(idEncoder.encode(comment.getId()));
        dto.setText(comment.getText());
        dto.setParentQuestionId(idEncoder.encode(comment.getParentContentId()));

        if (comment.getParentComment() != null) {
            dto.setParentCommentId(idEncoder.encode(comment.getParentComment().getId()));
        }

        dto.setAuthorAnonymousUsername(comment.getAuthor().getAnonymousUsername());
        dto.setDeleted(comment.isDeleted());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
        dto.setOwner(currentUserId != null && comment.getAuthor().getId().equals(currentUserId));

        return dto;
    }
}