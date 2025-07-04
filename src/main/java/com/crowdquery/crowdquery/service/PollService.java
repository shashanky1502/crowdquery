package com.crowdquery.crowdquery.service;

import com.crowdquery.crowdquery.dto.PaginatedResponseDto;
import com.crowdquery.crowdquery.dto.PollDto.PollRequestDto;
import com.crowdquery.crowdquery.dto.PollDto.PollResponseDto;
import com.crowdquery.crowdquery.dto.PollDto.PollVoteRequestDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PollService {

    PollResponseDto createPoll(PollRequestDto request);

    PaginatedResponseDto<PollResponseDto> getPollsByChannel(
            String channelCode, int limit, int offset, String sortBy, String sortDir);

    PollResponseDto getPollById(String pollId);

    @PreAuthorize("@pollServiceImpl.isPollOwner(#pollId)")
    void deletePoll(String pollId);

    PollResponseDto voteOnPoll(String pollId, PollVoteRequestDto request);

    PollResponseDto changeVote(String pollId, PollVoteRequestDto request);

    PollResponseDto removeVote(String pollId);

    // Utility methods for security
    boolean isPollOwner(String pollId);

    // Scheduled method to update expired polls
    void updateExpiredPolls();
}