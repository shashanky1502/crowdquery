package com.crowdquery.crowdquery.dto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.management.relation.Role;

@Data
public class UserDto {
    private UUID id;
    private String anonymousUsername;
    private Role role;
    private LocalDateTime lastUsernameChangeAt;
}

