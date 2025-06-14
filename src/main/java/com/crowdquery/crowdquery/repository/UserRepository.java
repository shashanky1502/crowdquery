package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    boolean existsByAnonymousUsername(String anonymousUsername);
}