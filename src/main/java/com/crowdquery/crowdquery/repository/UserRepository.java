package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByAnonymousUsername(String anonymousUsername);

    boolean existsByAnonymousUsername(String anonymousUsername);
}