package com.crowdquery.crowdquery.model;

import com.crowdquery.crowdquery.enums.ChannelRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "channel_memberships")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMembership {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    private ChannelRole role; // MODERATOR / PARTICIPANT

    private LocalDateTime joinedAt;
}

