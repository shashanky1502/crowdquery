package com.crowdquery.crowdquery.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.crowdquery.crowdquery.enums.CommentStatus;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    private UUID parentContentId; // Links to Question or Poll

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment; // Nullable for top-level comments

    @ManyToOne(fetch = FetchType.LAZY)
    private User author;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private CommentStatus status = CommentStatus.ACTIVE;

    @Builder.Default
    @Column(name = "is_edited", nullable = false)
    private boolean isEdited = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public int getCommentLevel() {
        int level = 0;
        Comment current = this;
        while (current.getParentComment() != null) {
            level++;
            current = current.getParentComment();
        }
        return level;
    }

    // Keep this for convenience
    public boolean isTopLevel() {
        return parentComment == null;
    }

}
