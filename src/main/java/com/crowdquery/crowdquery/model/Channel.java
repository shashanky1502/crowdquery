package com.crowdquery.crowdquery.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "channels")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Channel {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String channelCode;

    @ManyToOne(fetch = FetchType.LAZY)
    private User creator;

    @Builder.Default
    private String logoImageUrl = "https://www.redditstatic.com/shreddit/assets/hmm-snoo.png";

    @Builder.Default
    private boolean isReadOnly = false;

    @Builder.Default
    private boolean isDeleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
