package com.crowdquery.crowdquery.util;

import com.crowdquery.crowdquery.security.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SecurityUtil {

    /**
     * Get the current authenticated user's ID
     * This is what you'll use in your services
     */
    public static Optional<UUID> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal) {
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
            return Optional.of(userPrincipal.getUserId());
        }

        return Optional.empty();
    }

    /**
     * Get the current authenticated user principal
     * Use this when you need more than just the ID
     */
    public static Optional<CustomUserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal) {
            return Optional.of((CustomUserPrincipal) authentication.getPrincipal());
        }

        return Optional.empty();
    }

    /**
     * Get the current user's anonymous username
     */
    public static Optional<String> getCurrentUserAnonymousUsername() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::getAnonymousUsername);
    }

    /**
     * Check if the current user has a specific role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
        }

        return false;
    }
}