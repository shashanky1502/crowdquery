package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.Reaction;
import com.crowdquery.crowdquery.enums.ReactionTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    // Find user's reaction to a specific target
    Optional<Reaction> findByUserIdAndTargetIdAndTargetType(
            UUID userId, UUID targetId, ReactionTargetType targetType);

    // Get all reactions for a target with user details (for building the grouped
    // response)
    @Query("SELECT r FROM Reaction r " +
            "JOIN FETCH r.user u " +
            "WHERE r.targetId = :targetId AND r.targetType = :targetType " +
            "ORDER BY r.reactedAt DESC")
    List<Reaction> findReactionsWithUsersByTargetIdAndTargetType(
            @Param("targetId") UUID targetId,
            @Param("targetType") ReactionTargetType targetType);

    // Delete user's reaction
    void deleteByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, ReactionTargetType targetType);
}