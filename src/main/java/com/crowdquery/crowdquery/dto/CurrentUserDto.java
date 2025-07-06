package com.crowdquery.crowdquery.dto;

import com.crowdquery.crowdquery.enums.Role;

import lombok.Data;

@Data
public class CurrentUserDto {
    private String id; // UUID as String
    private String email;
    private String anonymousUsername;
    private String avatarUrl;
    private Role role;
}
