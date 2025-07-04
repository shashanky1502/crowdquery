package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, UUID> {

    // Check if user already voted on this poll
    Optional<PollVote> findByPollIdAndUserId(UUID pollId, UUID userId);

    // Count votes for a poll
    long countByPollId(UUID pollId);

    // Count votes for a specific option
    long countByPollOptionId(UUID pollOptionId);

    // Delete user's vote (for changing vote)
    void deleteByPollIdAndUserId(UUID pollId, UUID userId);
}