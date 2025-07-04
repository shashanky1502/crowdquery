package com.crowdquery.crowdquery.service.impl;

import com.crowdquery.crowdquery.dto.PaginatedResponseDto;
import com.crowdquery.crowdquery.dto.PollDto.*;
import com.crowdquery.crowdquery.enums.PollStatus;
import com.crowdquery.crowdquery.model.*;
import com.crowdquery.crowdquery.repository.*;
import com.crowdquery.crowdquery.service.PollService;
import com.crowdquery.crowdquery.service.ReactionService;
import com.crowdquery.crowdquery.util.IdEncoder;
import com.crowdquery.crowdquery.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollServiceImpl implements PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReactionService reactionService;
    private final IdEncoder idEncoder;

    @Override
    @Transactional
    public PollResponseDto createPoll(PollRequestDto request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        Channel channel = channelRepository.findByChannelCode(request.getChannelCode())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Channel not found"));

        // Validate expiry hours
        int expiryHours = request.getExpiryHours() != null ? request.getExpiryHours() : 24;
        if (expiryHours < 1 || expiryHours > 168) { // Max 7 days
            throw new ResponseStatusException(BAD_REQUEST, "Expiry hours must be between 1 and 168 (7 days)");
        }

        // Create poll
        Poll poll = Poll.builder()
                .question(request.getQuestion())
                .channel(channel)
                .author(author)
                .status(PollStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusHours(expiryHours))
                .build();

        Poll savedPoll = pollRepository.save(poll);

        // Create poll options
        for (int i = 0; i < request.getOptions().size(); i++) {
            PollOption option = PollOption.builder()
                    .text(request.getOptions().get(i))
                    .order(i + 1)
                    .poll(savedPoll)
                    .build();
            pollOptionRepository.save(option);
        }

        return mapToResponseDto(savedPoll);
    }

    @Override
    public PaginatedResponseDto<PollResponseDto> getPollsByChannel(
            String channelCode, int limit, int offset, String sortBy, String sortDir) {

        updateExpiredPolls();

        Channel channel = channelRepository.findByChannelCode(channelCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Channel not found"));

        // Create sort
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);

        // Create pageable
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);

        // Get polls
        Page<Poll> pollsPage = pollRepository.findByChannelIdOrderByCreatedAtDesc(channel.getId(), pageable);

        // Map to DTOs
        List<PollResponseDto> pollDtos = pollsPage.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return new PaginatedResponseDto<>(
                pollDtos,
                new PaginatedResponseDto.Pagination(
                        limit,
                        offset,
                        pollsPage.getTotalElements(),
                        pollsPage.hasNext()));
    }

    @Override
    public PollResponseDto getPollById(String pollId) {
        updateExpiredPolls();
        UUID decodedPollId = idEncoder.decode(pollId);
        Poll poll = pollRepository.findById(decodedPollId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Poll not found"));

        return mapToResponseDto(poll);
    }

    @Override
    @Transactional
    public void deletePoll(String pollId) {
        updateExpiredPolls();
        UUID decodedPollId = idEncoder.decode(pollId);
        Poll poll = pollRepository.findById(decodedPollId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Poll not found"));

        // Hard delete (CASCADE will handle options and votes)
        pollRepository.delete(poll);
        log.info("Poll deleted: {} by user: {}", pollId, SecurityUtil.getCurrentUserId().orElse(null));
    }

    @Override
    @Transactional
    public PollResponseDto voteOnPoll(String pollId, PollVoteRequestDto request) {
        updateExpiredPolls();
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        UUID decodedPollId = idEncoder.decode(pollId);
        Poll poll = pollRepository.findById(decodedPollId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Poll not found"));

        // Check if poll is still active and not expired
        if (!poll.canVote()) {
            throw new ResponseStatusException(BAD_REQUEST, "Poll is expired or inactive");
        }

        // Check if user already voted
        Optional<PollVote> existingVote = pollVoteRepository.findByPollIdAndUserId(decodedPollId, currentUserId);
        if (existingVote.isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "You have already voted on this poll. Use changeVote to modify your vote.");
        }

        // Get poll option
        UUID pollOptionId = idEncoder.decode(request.getPollOptionId());
        PollOption pollOption = pollOptionRepository.findById(pollOptionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Poll option not found"));

        // Verify option belongs to this poll
        if (!pollOption.getPoll().getId().equals(decodedPollId)) {
            throw new ResponseStatusException(BAD_REQUEST, "Poll option does not belong to this poll");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        // Create vote
        PollVote vote = PollVote.builder()
                .poll(poll)
                .pollOption(pollOption)
                .user(user)
                .build();

        pollVoteRepository.save(vote);

        return mapToResponseDto(poll);
    }

    @Override
    @Transactional
    public PollResponseDto changeVote(String pollId, PollVoteRequestDto request) {
        updateExpiredPolls();
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        UUID decodedPollId = idEncoder.decode(pollId);
        Poll poll = pollRepository.findById(decodedPollId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Poll not found"));

        // Check if poll is still active and not expired
        if (!poll.canVote()) {
            throw new ResponseStatusException(BAD_REQUEST, "Poll is expired or inactive");
        }

        // Check if user has voted
        PollVote existingVote = pollVoteRepository.findByPollIdAndUserId(decodedPollId, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                        "You haven't voted on this poll yet. Use vote endpoint instead."));

        // Get new poll option
        UUID newPollOptionId = idEncoder.decode(request.getPollOptionId());
        PollOption newPollOption = pollOptionRepository.findById(newPollOptionId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Poll option not found"));

        // Verify option belongs to this poll
        if (!newPollOption.getPoll().getId().equals(decodedPollId)) {
            throw new ResponseStatusException(BAD_REQUEST, "Poll option does not belong to this poll");
        }

        // Update vote
        existingVote.setPollOption(newPollOption);
        pollVoteRepository.save(existingVote);

        return mapToResponseDto(poll);
    }

    @Override
    @Transactional
    public PollResponseDto removeVote(String pollId) {
        updateExpiredPolls();
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        UUID decodedPollId = idEncoder.decode(pollId);
        Poll poll = pollRepository.findById(decodedPollId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Poll not found"));

        // Check if poll is still active and not expired
        if (!poll.canVote()) {
            throw new ResponseStatusException(BAD_REQUEST, "Poll is expired or inactive");
        }

        // Remove user's vote
        pollVoteRepository.deleteByPollIdAndUserId(decodedPollId, currentUserId);

        return mapToResponseDto(poll);
    }

    @Override
    public boolean isPollOwner(String pollId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
        if (currentUserId == null)
            return false;

        UUID decodedPollId = idEncoder.decode(pollId);
        return pollRepository.findById(decodedPollId)
                .map(poll -> poll.getAuthor().getId().equals(currentUserId))
                .orElse(false);
    }

    @Override
    @Transactional 
    public void updateExpiredPolls() {
        try {
            int updatedCount = pollRepository.updateExpiredPollsStatus(
                    LocalDateTime.now(), PollStatus.ACTIVE, PollStatus.EXPIRED);
            
            if (updatedCount > 0) {
                log.debug("Updated {} expired polls", updatedCount);
            }
        } catch (Exception e) {
            log.error("Error updating expired polls: {}", e.getMessage(), e);
        }
    }

    private PollResponseDto mapToResponseDto(Poll poll) {
        PollResponseDto dto = new PollResponseDto();
        dto.setId(idEncoder.encode(poll.getId()));
        dto.setQuestion(poll.getQuestion());
        dto.setChannelCode(poll.getChannel().getChannelCode());
        dto.setAuthorAnonymousUsername(poll.getAuthor().getAnonymousUsername());
        dto.setStatus(poll.getStatus());
        dto.setExpiresAt(poll.getExpiresAt());
        dto.setCreatedAt(poll.getCreatedAt());
        dto.setUpdatedAt(poll.getUpdatedAt());

        UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
        dto.setOwner(currentUserId != null && poll.getAuthor().getId().equals(currentUserId));
        dto.setCanVote(poll.canVote());

        // Get total votes
        long totalVotes = pollVoteRepository.countByPollId(poll.getId());
        dto.setTotalVotes((int) totalVotes);

        // Get user's vote
        String userVote = null;
        if (currentUserId != null) {
            Optional<PollVote> userVoteOpt = pollVoteRepository.findByPollIdAndUserId(poll.getId(), currentUserId);
            if (userVoteOpt.isPresent()) {
                userVote = idEncoder.encode(userVoteOpt.get().getPollOption().getId());
            }
        }
        dto.setUserVote(userVote);

        // Map options
        List<PollOption> options = pollOptionRepository.findByPollIdOrderByOrderAsc(poll.getId());
        List<PollOptionResponseDto> optionDtos = options.stream()
                .map(option -> mapToOptionResponseDto(option, totalVotes, currentUserId))
                .collect(Collectors.toList());
        dto.setOptions(optionDtos);

        // Get reactions
        String encodedPollId = idEncoder.encode(poll.getId());
        dto.setReactions(reactionService.getReactionSummary(encodedPollId, "POLL"));

        return dto;
    }

    private PollOptionResponseDto mapToOptionResponseDto(PollOption option, long totalVotes, UUID currentUserId) {
        PollOptionResponseDto dto = new PollOptionResponseDto();
        dto.setId(idEncoder.encode(option.getId()));
        dto.setText(option.getText());
        dto.setOrder(option.getOrder());

        long voteCount = pollVoteRepository.countByPollOptionId(option.getId());
        dto.setVoteCount((int) voteCount);

        // Calculate percentage
        double percentage = totalVotes > 0 ? (double) voteCount / totalVotes * 100 : 0;
        dto.setPercentage(Math.round(percentage * 100.0) / 100.0); // Round to 2 decimal places

        // Check if current user voted for this option
        boolean userVoted = false;
        if (currentUserId != null) {
            Optional<PollVote> userVote = pollVoteRepository.findByPollIdAndUserId(option.getPoll().getId(),
                    currentUserId);
            userVoted = userVote.isPresent() && userVote.get().getPollOption().getId().equals(option.getId());
        }
        dto.setUserVoted(userVoted);

        return dto;
    }
}