package com.crowdquery.crowdquery.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "poll_votes")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PollVote {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    private PollOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private LocalDateTime votedAt;
}

