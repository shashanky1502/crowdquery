package com.crowdquery.crowdquery.service.impl;

import com.crowdquery.crowdquery.dto.CommentDto.CommentRequestDto;
import com.crowdquery.crowdquery.dto.CommentDto.CommentResponseDto;
import com.crowdquery.crowdquery.dto.CommentDto.CommentUpdateDto;
import com.crowdquery.crowdquery.dto.PaginatedResponseDto;
import com.crowdquery.crowdquery.enums.CommentStatus;
import com.crowdquery.crowdquery.model.*;
import com.crowdquery.crowdquery.repository.*;
import com.crowdquery.crowdquery.service.CommentService;
import com.crowdquery.crowdquery.service.ReactionService;
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
    private final ReactionService reactionService;
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
                .author(author)
                .status(CommentStatus.ACTIVE);

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
            String questionId, int limit, int offset, String sortBy, String sortDir) {

        UUID parentQuestionId = idEncoder.decode(questionId);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");

        // Convert offset and limit to page and size for Spring Data
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit, sort);

        // Get only TOP-LEVEL comments (no parent comment)
        Page<Comment> commentsPage = commentRepository.findByParentContentIdAndParentCommentIsNull(
                parentQuestionId, pageable);

        return new PaginatedResponseDto<>(
                commentsPage.getContent().stream()
                        .map(this::mapToResponseDto)
                        .toList(),
                new PaginatedResponseDto.Pagination(
                        limit,
                        offset,
                        commentsPage.getTotalElements(),
                        commentsPage.hasNext()));
    }

    @Override
    public PaginatedResponseDto<CommentResponseDto> getRepliesByComment(
            String commentId, int limit, int offset, String sortBy, String sortDir) {

        UUID parentCommentId = idEncoder.decode(commentId);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");

        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit, sort);

        // Get replies to this comment
        Page<Comment> repliesPage = commentRepository.findByParentCommentIdAndStatusNot(
                parentCommentId, CommentStatus.DELETED, pageable);

        return new PaginatedResponseDto<>(
                repliesPage.getContent().stream()
                        .map(this::mapToResponseDto)
                        .toList(),
                new PaginatedResponseDto.Pagination(
                        limit,
                        offset,
                        repliesPage.getTotalElements(),
                        repliesPage.hasNext()));
    }

    @Override
    @Transactional
    public CommentResponseDto updateComment(String commentId, CommentUpdateDto request) {
        UUID realCommentId = idEncoder.decode(commentId);
        Comment comment = commentRepository.findById(realCommentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Comment not found"));

        if (!comment.getText().equals(request.getText().trim())) {
            comment.setText(request.getText().trim());
            comment.setEdited(true); // Mark as edited
        }
        Comment saved = commentRepository.save(comment);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteComment(String commentId) {
        UUID realCommentId = idEncoder.decode(commentId);
        Comment comment = commentRepository.findById(realCommentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Comment not found"));

        comment.setStatus(CommentStatus.DELETED);
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
            return channelMembershipRepository
                    .findByChannelIdAndUserId(parentQuestion.getChannel().getId(), currentUserId)
                    .map(membership -> membership.getRole() == com.crowdquery.crowdquery.enums.ChannelRole.MODERATOR)
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    private CommentResponseDto mapToResponseDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(idEncoder.encode(comment.getId()));

        // Show "[deleted]" text for deleted comments
        if (comment.getStatus() == CommentStatus.DELETED) {
            dto.setText("[deleted]");
            dto.setAuthorAnonymousUsername("[deleted user]");
        } else {
            dto.setText(comment.getText());
            dto.setAuthorAnonymousUsername(comment.getAuthor().getAnonymousUsername());
        }

        dto.setParentQuestionId(idEncoder.encode(comment.getParentContentId()));

        if (comment.getParentComment() != null) {
            dto.setParentCommentId(idEncoder.encode(comment.getParentComment().getId()));
        }

        // Set comment level using the helper method from Comment entity
        dto.setCommentLevel(comment.getCommentLevel());

        // Count replies for this comment
        long replyCount = commentRepository.countByParentCommentIdAndStatusNot(
                comment.getId(), CommentStatus.DELETED);
        dto.setReplyCount((int) replyCount);

        dto.setStatus(comment.getStatus());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setEdited(comment.isEdited());

        UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
        dto.setOwner(currentUserId != null && comment.getAuthor().getId().equals(currentUserId));
        dto.setReactions(reactionService.getReactionSummary(idEncoder.encode(comment.getId()), "COMMENT"));

        return dto;
    }
}