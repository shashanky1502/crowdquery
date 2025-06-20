package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.Reaction;
import com.crowdquery.crowdquery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
    List<Reaction> findByUser(User user);
}
