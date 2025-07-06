package com.crowdquery.crowdquery.service.impl;

import com.crowdquery.crowdquery.dto.PaginatedResponseDto;
import com.crowdquery.crowdquery.dto.QuestionDto.QuestionRequestDto;
import com.crowdquery.crowdquery.dto.QuestionDto.QuestionResponseDto;
import com.crowdquery.crowdquery.enums.CommentStatus;
import com.crowdquery.crowdquery.enums.QuestionStatus;
import com.crowdquery.crowdquery.model.*;
import com.crowdquery.crowdquery.repository.*;
import com.crowdquery.crowdquery.service.ImageUploadService;
import com.crowdquery.crowdquery.service.QuestionService;
import com.crowdquery.crowdquery.service.ReactionService;
import com.crowdquery.crowdquery.util.IdEncoder;
import com.crowdquery.crowdquery.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final ModelMapper modelMapper;
    private final QuestionRepository questionRepository;
    private final QuestionImageRepository questionImageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMembershipRepository channelMembershipRepository;
    private final CommentRepository commentRepository;
    private final IdEncoder idEncoder;
    private final ReactionService reactionService;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional
    public QuestionResponseDto createQuestionWithImages(QuestionRequestDto request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        Channel channel = channelRepository.findByChannelCode(request.getChannelCode())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Channel not found"));

        if (!channelMembershipRepository.existsByChannelAndUser(channel, author)) {
            throw new ResponseStatusException(FORBIDDEN, "You are not a member of this channel");
        }

        // Create question first
        Question question = Question.builder()
                .text(request.getText())
                .channel(channel)
                .author(author)
                .build();

        Question savedQuestion = questionRepository.save(question);

        // Upload and save images if provided
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (int i = 0; i < request.getImages().size(); i++) {
                MultipartFile image = request.getImages().get(i);
                try {
                    String imageUrl = imageUploadService.uploadImage(image);

                    QuestionImage questionImage = QuestionImage.builder()
                            .question(savedQuestion)
                            .imageUrl(imageUrl)
                            .originalFilename(image.getOriginalFilename())
                            .fileSize(image.getSize())
                            .imageOrder(i + 1)
                            .build();

                    questionImageRepository.save(questionImage);

                } catch (Exception e) {
                    log.error("Failed to upload image: {}", e.getMessage());
                    throw new ResponseStatusException(INTERNAL_SERVER_ERROR,
                            "Failed to upload image: " + e.getMessage());
                }
            }
        }

        return mapToResponseDto(savedQuestion);
    }

    @Override
    public PaginatedResponseDto<QuestionResponseDto> getQuestionsByChannel(
            String channelCode, int limit, int offset, String sortBy, String sortDir) {

        Channel channel = channelRepository.findByChannelCode(channelCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Channel not found"));

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");

        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit, sort);

        Page<Question> questionsPage = questionRepository.findByChannelAndStatusNot(
                channel, QuestionStatus.DELETED, pageable);

        return new PaginatedResponseDto<>(
                questionsPage.getContent().stream()
                        .map(this::mapToResponseDto)
                        .toList(),
                new PaginatedResponseDto.Pagination(
                        limit,
                        offset,
                        questionsPage.getTotalElements(),
                        questionsPage.hasNext()));
    }

    @Override
    public QuestionResponseDto getQuestion(String questionId) {
        UUID realQuestionId = idEncoder.decode(questionId);
        Question question = questionRepository.findById(realQuestionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Question not found"));

        UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
        if (currentUserId != null) {
            User user = userRepository.findById(currentUserId).orElse(null);
            if (user != null && !channelMembershipRepository.existsByChannelAndUser(question.getChannel(), user)) {
                throw new ResponseStatusException(FORBIDDEN, "You are not a member of this channel");
            }
        }

        return mapToResponseDto(question);
    }

    @Override
    @Transactional
    public QuestionResponseDto updateQuestion(String questionId, QuestionRequestDto request) {
        UUID realQuestionId = idEncoder.decode(questionId);
        Question question = questionRepository.findById(realQuestionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Question not found"));

        // Update text
        question.setText(request.getText());

        // Handle image updates if provided
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // Remove existing images
            questionImageRepository.deleteByQuestionId(realQuestionId);

            // Add new images
            for (int i = 0; i < request.getImages().size(); i++) {
                MultipartFile image = request.getImages().get(i);
                try {
                    String imageUrl = imageUploadService.uploadImage(image);

                    QuestionImage questionImage = QuestionImage.builder()
                            .question(question)
                            .imageUrl(imageUrl)
                            .originalFilename(image.getOriginalFilename())
                            .fileSize(image.getSize())
                            .imageOrder(i + 1)
                            .build();

                    questionImageRepository.save(questionImage);

                } catch (Exception e) {
                    throw new ResponseStatusException(INTERNAL_SERVER_ERROR,
                            "Failed to upload image: " + e.getMessage());
                }
            }
        }

        Question saved = questionRepository.save(question);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteQuestion(String questionId) {
        UUID realQuestionId = idEncoder.decode(questionId);
        Question question = questionRepository.findById(realQuestionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Question not found"));

        question.setStatus(QuestionStatus.DELETED);
        questionRepository.save(question);
    }

    @Override
    public PaginatedResponseDto<QuestionResponseDto> getMyQuestions(
            int limit, int offset, String sortBy, String sortDir) {
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");

        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit, sort);

        Page<Question> questionsPage = questionRepository.findByAuthorIdAndStatusNot(
                currentUserId, QuestionStatus.DELETED, pageable);

        return new PaginatedResponseDto<>(
                questionsPage.getContent().stream()
                        .map(this::mapToResponseDto)
                        .toList(),
                new PaginatedResponseDto.Pagination(
                        limit,
                        offset,
                        questionsPage.getTotalElements(),
                        questionsPage.hasNext()));
    }

    @Override
    public PaginatedResponseDto<QuestionResponseDto> getQuestionsByUser(
            String username, int limit, int offset, String sortBy, String sortDir) {

        User user = userRepository.findByAnonymousUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");

        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit, sort);

        Page<Question> questionsPage = questionRepository.findByAuthorIdAndStatusNot(
                user.getId(), QuestionStatus.DELETED, pageable);

        return new PaginatedResponseDto<>(
                questionsPage.getContent().stream()
                        .map(this::mapToResponseDto)
                        .toList(),
                new PaginatedResponseDto.Pagination(
                        limit,
                        offset,
                        questionsPage.getTotalElements(),
                        questionsPage.hasNext()));
    }

    @Override
    public boolean isQuestionOwner(String questionId) {
        try {
            UUID realQuestionId = idEncoder.decode(questionId);
            UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
            if (currentUserId == null)
                return false;

            Question question = questionRepository.findById(realQuestionId).orElse(null);
            return question != null && question.getAuthor().getId().equals(currentUserId);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isQuestionOwnerOrChannelModerator(String questionId) {
        try {
            UUID realQuestionId = idEncoder.decode(questionId);
            UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
            if (currentUserId == null)
                return false;

            Question question = questionRepository.findById(realQuestionId).orElse(null);
            if (question == null)
                return false;

            // Check if owner
            if (question.getAuthor().getId().equals(currentUserId)) {
                return true;
            }

            // Check if channel moderator
            return channelMembershipRepository.findByChannelIdAndUserId(question.getChannel().getId(), currentUserId)
                    .map(membership -> membership.getRole() == com.crowdquery.crowdquery.enums.ChannelRole.MODERATOR)
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    private QuestionResponseDto mapToResponseDto(Question question) {
        QuestionResponseDto dto = modelMapper.map(question, QuestionResponseDto.class);

        dto.setId(idEncoder.encode(question.getId()));
        dto.setChannelCode(question.getChannel().getChannelCode());
        dto.setChannelName(question.getChannel().getName());
        dto.setAuthorAnonymousUsername(question.getAuthor().getAnonymousUsername());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());

        // Get image URLs from separate table
        dto.setImageUrls(questionImageRepository.findImageUrlsByQuestionId(question.getId()));

        dto.setCommentCount(
                commentRepository.countByParentContentIdAndStatusNot(question.getId(), CommentStatus.DELETED));

        UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
        dto.setOwner(currentUserId != null && question.getAuthor().getId().equals(currentUserId));

        dto.setReactions(reactionService.getReactionSummary(idEncoder.encode(question.getId()), "QUESTION"));

        return dto;
    }
}