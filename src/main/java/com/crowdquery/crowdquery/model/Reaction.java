package com.crowdquery.crowdquery.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reactions")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Reaction {

    @Id @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private UUID targetId; // Question, Comment, or Poll

    private String emoji;

    private LocalDateTime reactedAt;
}

