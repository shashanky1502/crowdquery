package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.Poll;
import com.crowdquery.crowdquery.model.PollVote;
import com.crowdquery.crowdquery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, UUID> {
    Optional<PollVote> findByPollAndUser(Poll poll, User user);
}

