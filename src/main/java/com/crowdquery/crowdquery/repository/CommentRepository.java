package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Basic queries for parent content (Question/Poll)
    Page<Comment> findByParentContentIdAndIsDeletedFalse(UUID parentContentId, Pageable pageable);

    // Count comments for a parent content
    long countByParentContentIdAndIsDeletedFalse(UUID parentContentId);

    // Find top-level comments (no parent comment)
    Page<Comment> findByParentContentIdAndParentCommentIsNullAndIsDeletedFalse(
            UUID parentContentId, Pageable pageable);

    // Find replies to a specific comment
    Page<Comment> findByParentCommentIdAndIsDeletedFalse(UUID parentCommentId, Pageable pageable);

    // Count replies to a specific comment
    long countByParentCommentIdAndIsDeletedFalse(UUID parentCommentId);

    // Find comments by author
    Page<Comment> findByAuthorIdAndIsDeletedFalse(UUID authorId, Pageable pageable);

    // All comments (including deleted) - for admin purposes
    Page<Comment> findByParentContentId(UUID parentContentId, Pageable pageable);


    // Search in comments
    @Query("SELECT c FROM Comment c WHERE c.parentContentId = :parentContentId " +
            "AND c.isDeleted = false " +
            "AND (:searchText IS NULL OR LOWER(c.text) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<Comment> findByParentContentIdWithSearch(@Param("parentContentId") UUID parentContentId,
            @Param("searchText") String searchText,
            Pageable pageable);

    // Find comments mentioning a user
    @Query("SELECT c FROM Comment c WHERE c.parentContentId = :parentContentId " +
            "AND c.isDeleted = false " +
            "AND LOWER(c.text) LIKE LOWER(CONCAT('%@', :username, '%'))")
    Page<Comment> findCommentsWithMention(@Param("parentContentId") UUID parentContentId,
            @Param("username") String username,
            Pageable pageable);

    // Check if comment exists and is not deleted
    boolean existsByIdAndIsDeletedFalse(UUID id);

}