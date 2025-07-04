package com.crowdquery.crowdquery.service.impl;

import com.crowdquery.crowdquery.dto.ReactionDto.ReactionRequestDto;
import com.crowdquery.crowdquery.dto.ReactionDto.ReactionSummaryDto;
import com.crowdquery.crowdquery.enums.ReactionTargetType;
import com.crowdquery.crowdquery.model.Reaction;
import com.crowdquery.crowdquery.model.User;
import com.crowdquery.crowdquery.repository.ReactionRepository;
import com.crowdquery.crowdquery.repository.UserRepository;
import com.crowdquery.crowdquery.repository.QuestionRepository;
import com.crowdquery.crowdquery.repository.CommentRepository;
import com.crowdquery.crowdquery.repository.PollRepository;
import com.crowdquery.crowdquery.service.ReactionService;
import com.crowdquery.crowdquery.util.IdEncoder;
import com.crowdquery.crowdquery.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final CommentRepository commentRepository;
    private final PollRepository pollRepository;
    private final IdEncoder idEncoder;

    @Override
    @Transactional
    public ReactionSummaryDto addOrUpdateReaction(ReactionRequestDto request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        UUID targetId = idEncoder.decode(request.getTargetId());
        ReactionTargetType targetType = request.getTargetType();

        // Validate target exists
        validateTargetExists(targetId, targetType);

        // Validate emoji
        validateEmoji(request.getEmoji());

        // Check if user already has a reaction on this target
        Optional<Reaction> existingReaction = reactionRepository
                .findByUserIdAndTargetIdAndTargetType(currentUserId, targetId, targetType);

        if (existingReaction.isPresent()) {
            // Update existing reaction
            Reaction reaction = existingReaction.get();
            reaction.setEmoji(request.getEmoji().trim());
            reactionRepository.save(reaction);
        } else {
            // Create new reaction
            Reaction newReaction = Reaction.builder()
                    .user(user)
                    .targetId(targetId)
                    .targetType(targetType)
                    .emoji(request.getEmoji().trim())
                    .build();
            reactionRepository.save(newReaction);
        }

        // Return updated reaction summary
        return buildReactionSummary(targetId, targetType, currentUserId);
    }

    @Override
    @Transactional
    public ReactionSummaryDto removeReaction(String targetId, String targetType) {
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        UUID decodedTargetId = idEncoder.decode(targetId);
        ReactionTargetType reactionTargetType = ReactionTargetType.valueOf(targetType.toUpperCase());

        // Validate target exists
        validateTargetExists(decodedTargetId, reactionTargetType);

        // Remove user's reaction
        reactionRepository.deleteByUserIdAndTargetIdAndTargetType(
                currentUserId, decodedTargetId, reactionTargetType);

        // Return updated reaction summary
        return buildReactionSummary(decodedTargetId, reactionTargetType, currentUserId);
    }

    @Override
    public ReactionSummaryDto getReactionSummary(String targetId, String targetType) {
        UUID decodedTargetId = idEncoder.decode(targetId);
        ReactionTargetType reactionTargetType = ReactionTargetType.valueOf(targetType.toUpperCase());

        // Validate target exists
        validateTargetExists(decodedTargetId, reactionTargetType);

        UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
        return buildReactionSummary(decodedTargetId, reactionTargetType, currentUserId);
    }

    private ReactionSummaryDto buildReactionSummary(UUID targetId, ReactionTargetType targetType, UUID currentUserId) {
        // Get all reactions with user details
        List<Reaction> reactions = reactionRepository
                .findReactionsWithUsersByTargetIdAndTargetType(targetId, targetType);

        // Get current user's reaction
        String userReaction = null;
        if (currentUserId != null) {
            Optional<Reaction> userReactionOpt = reactions.stream()
                    .filter(r -> r.getUser().getId().equals(currentUserId))
                    .findFirst();
            if (userReactionOpt.isPresent()) {
                userReaction = userReactionOpt.get().getEmoji();
            }
        }

        // Group reactions by emoji
        Map<String, List<Reaction>> groupedByEmoji = reactions.stream()
                .collect(Collectors.groupingBy(Reaction::getEmoji));

        // Build emoji reaction map
        Map<String, ReactionSummaryDto.EmojiReactionDto> emojiReactions = new LinkedHashMap<>();

        for (Map.Entry<String, List<Reaction>> entry : groupedByEmoji.entrySet()) {
            String emoji = entry.getKey();
            List<Reaction> emojiReactionList = entry.getValue();

            // Map users for this emoji
            List<ReactionSummaryDto.UserReactionDto> users = emojiReactionList.stream()
                    .map(reaction -> {
                        ReactionSummaryDto.UserReactionDto userDto = new ReactionSummaryDto.UserReactionDto();
                        userDto.setUserAnonymousUsername(reaction.getUser().getAnonymousUsername());
                        userDto.setReactedAt(reaction.getReactedAt());
                        userDto.setCurrentUser(
                                currentUserId != null && reaction.getUser().getId().equals(currentUserId));
                        return userDto;
                    })
                    .sorted((a, b) -> b.getReactedAt().compareTo(a.getReactedAt())) // Latest first
                    .collect(Collectors.toList());

            ReactionSummaryDto.EmojiReactionDto emojiDto = new ReactionSummaryDto.EmojiReactionDto();
            emojiDto.setCount(users.size());
            emojiDto.setUsers(users);

            emojiReactions.put(emoji, emojiDto);
        }

        ReactionSummaryDto summary = new ReactionSummaryDto();
        summary.setReactions(emojiReactions);
        summary.setUserReaction(userReaction);

        return summary;
    }

    private void validateTargetExists(UUID targetId, ReactionTargetType targetType) {
        switch (targetType) {
            case QUESTION:
                if (!questionRepository.existsById(targetId)) {
                    throw new ResponseStatusException(NOT_FOUND, "Question not found");
                }
                break;
            case COMMENT:
                if (!commentRepository.existsById(targetId)) {
                    throw new ResponseStatusException(NOT_FOUND, "Comment not found");
                }
                break;
            case POLL:
                if (!pollRepository.existsById(targetId)) {
                    throw new ResponseStatusException(NOT_FOUND, "Poll not found");
                }
                break;
            default:
                throw new ResponseStatusException(BAD_REQUEST, "Invalid target type");
        }
    }

    private void validateEmoji(String emoji) {
        if (emoji == null || emoji.trim().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Emoji cannot be empty");
        }

        String trimmedEmoji = emoji.trim();

        // Simple check: if it contains ASCII letters (a-z, A-Z), reject it
        if (trimmedEmoji.matches(".*[a-zA-Z].*")) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Only emojis are allowed. Text reactions are not permitted.");
        }

        // Additional length check
        if (trimmedEmoji.length() > 20) {
            throw new ResponseStatusException(BAD_REQUEST, "Emoji reaction too long. Maximum 20 characters allowed.");
        }

        // Check if it's not just spaces or special characters
        if (trimmedEmoji.matches("^[\\s\\p{Punct}]+$")) {
            throw new ResponseStatusException(BAD_REQUEST, "Please use actual emojis for reactions.");
        }
    }
}