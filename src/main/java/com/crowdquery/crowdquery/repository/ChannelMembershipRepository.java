package com.crowdquery.crowdquery.repository;

import com.crowdquery.crowdquery.model.Channel;
import com.crowdquery.crowdquery.model.ChannelMembership;
import com.crowdquery.crowdquery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelMembershipRepository extends JpaRepository<ChannelMembership, UUID> {
    boolean existsByChannelAndUser(Channel channel, User user);

    boolean existsByChannelIdAndUserId(UUID channelId, UUID userId);

    Optional<ChannelMembership> findByChannelAndUser(Channel channel, User user);

    Optional<ChannelMembership> findByChannelIdAndUserId(UUID channelId, UUID userId);

    List<ChannelMembership> findAllByUser(User user);
}
