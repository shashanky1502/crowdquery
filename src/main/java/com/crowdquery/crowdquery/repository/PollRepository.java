package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.Poll;
import com.crowdquery.crowdquery.enums.PollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PollRepository extends JpaRepository<Poll, UUID> {

    // Find polls by channel
    Page<Poll> findByChannelIdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

    // Find active polls by channel
    Page<Poll> findByChannelIdAndStatusOrderByCreatedAtDesc(UUID channelId, PollStatus status, Pageable pageable);

    // Find polls by author
    Page<Poll> findByAuthorIdOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

    // Find expired polls that need status update
    @Query("SELECT p FROM Poll p WHERE p.expiresAt < :now AND p.status = :activeStatus")
    List<Poll> findExpiredPolls(@Param("now") LocalDateTime now, @Param("activeStatus") PollStatus activeStatus);

    // Update expired polls status
    @Modifying
    @Query("UPDATE Poll p SET p.status = :expiredStatus WHERE p.expiresAt < :now AND p.status = :activeStatus")
    int updateExpiredPollsStatus(@Param("now") LocalDateTime now,
            @Param("activeStatus") PollStatus activeStatus,
            @Param("expiredStatus") PollStatus expiredStatus);

    // Count polls by channel
    long countByChannelId(UUID channelId);
}