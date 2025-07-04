package com.crowdquery.crowdquery.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.crowdquery.crowdquery.enums.ReactionTargetType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reactions", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "target_id",
        "target_type" }))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reaction {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReactionTargetType targetType;

    @Column(nullable = false)
    private String emoji;

    @CreationTimestamp
    @Column(name = "reacted_at")
    private LocalDateTime reactedAt;
}