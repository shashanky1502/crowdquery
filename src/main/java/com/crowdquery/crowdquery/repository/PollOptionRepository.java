package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, UUID> {

    List<PollOption> findByPollIdOrderByOrderAsc(UUID pollId);
}