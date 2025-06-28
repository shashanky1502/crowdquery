package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.enums.QuestionStatus;
import com.crowdquery.crowdquery.model.Channel;
import com.crowdquery.crowdquery.model.Question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    // Basic channel queries
    List<Question> findByChannel(Channel channel);

    // Paginated queries with status filtering
    Page<Question> findByChannelAndStatusNot(Channel channel, QuestionStatus status, Pageable pageable);

    // Active questions only
    Page<Question> findByChannelAndStatus(Channel channel, QuestionStatus status, Pageable pageable);

    // Count questions by channel
    long countByChannelAndStatusNot(Channel channel, QuestionStatus status);

    // Find by author
    Page<Question> findByAuthorIdAndStatusNot(UUID authorId, QuestionStatus status, Pageable pageable);

    // Custom queries for advanced filtering
    @Query("SELECT q FROM Question q WHERE q.channel = :channel AND q.status != :excludeStatus " +
            "AND (:searchText IS NULL OR LOWER(q.text) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<Question> findByChannelWithSearch(@Param("channel") Channel channel,
            @Param("excludeStatus") QuestionStatus excludeStatus,
            @Param("searchText") String searchText,
            Pageable pageable);

    // Check if question exists and is active
    boolean existsByIdAndStatusNot(UUID id, QuestionStatus status);
}
