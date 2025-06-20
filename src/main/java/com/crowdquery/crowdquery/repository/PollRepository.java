package com.crowdquery.crowdquery.repository;


import com.crowdquery.crowdquery.model.Channel;
import com.crowdquery.crowdquery.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PollRepository extends JpaRepository<Poll, UUID> {
    List<Poll> findByChannel(Channel channel);
}
