package com.crowdquery.crowdquery.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "poll_options")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PollOption {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Poll poll;

    private String text;
}

