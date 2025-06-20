package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {
    Optional<Channel> findByChannelCode(String channelCode);

    boolean existsByChannelCode(String channelCode);
}
