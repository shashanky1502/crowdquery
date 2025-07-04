package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.enums.CommentStatus;
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

        // Count comments for a parent content
        long countByParentContentIdAndStatusNot(UUID parentContentId, CommentStatus status);

        // Find top-level comments (no parent comment) - INCLUDES DELETED for now
        Page<Comment> findByParentContentIdAndParentCommentIsNull(UUID parentContentId, Pageable pageable);


        // Find replies to a specific comment
        Page<Comment> findByParentCommentIdAndStatusNot(UUID parentCommentId, CommentStatus status, Pageable pageable);

        // Count replies to a specific comment
        long countByParentCommentIdAndStatusNot(UUID parentCommentId, CommentStatus status);

        // Find comments by author
        Page<Comment> findByAuthorIdAndStatusNot(UUID authorId, CommentStatus status, Pageable pageable);


        // Search in comments
        @Query("SELECT c FROM Comment c WHERE c.parentContentId = :parentContentId " +
                        "AND c.status != :deletedStatus " +
                        "AND (:searchText IS NULL OR LOWER(c.text) LIKE LOWER(CONCAT('%', :searchText, '%')))")
        Page<Comment> findByParentContentIdWithSearch(@Param("parentContentId") UUID parentContentId,
                        @Param("searchText") String searchText,
                        @Param("deletedStatus") CommentStatus deletedStatus,
                        Pageable pageable);

        // Find comments mentioning a user
        @Query("SELECT c FROM Comment c WHERE c.parentContentId = :parentContentId " +
                        "AND c.status != :deletedStatus " +
                        "AND LOWER(c.text) LIKE LOWER(CONCAT('%@', :username, '%'))")
        Page<Comment> findCommentsWithMention(@Param("parentContentId") UUID parentContentId,
                        @Param("username") String username,
                        @Param("deletedStatus") CommentStatus deletedStatus,
                        Pageable pageable);

}