package com.crowdquery.crowdquery.security;

import com.crowdquery.crowdquery.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class CustomUserPrincipal implements UserDetails {

    private final User user;

    public UUID getUserId() {
        return user.getId();
    }

    public String getAnonymousUsername() {
        return user.getAnonymousUsername();
    }

    public String getAvatarUrl() {
        return user.getAvatarUrl();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return null; // We don't store passwords since we use OAuth2
    }

    @Override
    public String getUsername() {
        return user.getId().toString(); // Use UUID as username for Spring Security
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}