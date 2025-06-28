package com.crowdquery.crowdquery.util;

import org.springframework.stereotype.Component;
import java.util.Base64;
import java.util.UUID;

@Component
public class IdEncoder {

    private static final String SALT = "crowdquery2024"; // @Refactor need toUse a proper secret in production

    public String encode(UUID id) {
        String combined = id.toString() + ":" + SALT;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(combined.getBytes());
    }

    public UUID decode(String encodedId) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(encodedId));
            String[] parts = decoded.split(":");
            if (parts.length == 2 && SALT.equals(parts[1])) {
                return UUID.fromString(parts[0]);
            }
            throw new IllegalArgumentException("Invalid encoded ID");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid encoded ID format");
        }
    }
}