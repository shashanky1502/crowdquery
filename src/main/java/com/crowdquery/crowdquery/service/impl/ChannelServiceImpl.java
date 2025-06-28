package com.crowdquery.crowdquery.service.impl;

import com.crowdquery.crowdquery.dto.ChannelDto.ChannelRequestDto;
import com.crowdquery.crowdquery.dto.ChannelDto.ChannelResponseDto;
import com.crowdquery.crowdquery.enums.ChannelRole;
import com.crowdquery.crowdquery.model.*;
import com.crowdquery.crowdquery.repository.*;
import com.crowdquery.crowdquery.service.ChannelService;
import com.crowdquery.crowdquery.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

        private final ChannelRepository channelRepository;
        private final UserRepository userRepository;
        private final ChannelMembershipRepository channelMembershipRepository;
        private final ModelMapper modelMapper;

        @Override
        @Transactional
        public ChannelResponseDto createChannel(ChannelRequestDto request) {
                UUID currentUserId = SecurityUtil.getCurrentUserId()
                                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

                User creator = userRepository.findById(currentUserId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

                String channelCode = generateUniqueChannelCode();

                Channel channel = Channel.builder()
                                .name(request.getName())
                                .channelCode(channelCode)
                                .creator(creator)
                                .logoImageUrl(request.getLogoImageUrl())
                                .build();

                Channel saved = channelRepository.save(channel);

                // Add creator as moderator
                ChannelMembership membership = ChannelMembership.builder()
                                .channel(saved)
                                .user(creator)
                                .role(ChannelRole.MODERATOR)
                                .joinedAt(saved.getCreatedAt())
                                .build();
                channelMembershipRepository.save(membership);

                return modelMapper.map(saved, ChannelResponseDto.class);
        }

        @Override
        @Transactional
        public ChannelResponseDto joinChannel(String channelCode) {
                UUID currentUserId = SecurityUtil.getCurrentUserId()
                                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

                Channel channel = channelRepository.findByChannelCode(channelCode)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Channel not found"));

                User user = userRepository.findById(currentUserId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

                if (channelMembershipRepository.existsByChannelAndUser(channel, user)) {
                        throw new ResponseStatusException(BAD_REQUEST, "Already a member");
                }

                ChannelMembership membership = ChannelMembership.builder()
                                .channel(channel)
                                .user(user)
                                .role(ChannelRole.PARTICIPANT)
                                .joinedAt(java.time.LocalDateTime.now())
                                .build();
                channelMembershipRepository.save(membership);

                return modelMapper.map(channel, ChannelResponseDto.class);
        }

        @Override
        @Transactional
        public void leaveChannel(UUID channelId) {
                UUID currentUserId = SecurityUtil.getCurrentUserId()
                                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

                Channel channel = channelRepository.findById(channelId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Channel not found"));

                User user = userRepository.findById(currentUserId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

                ChannelMembership membership = channelMembershipRepository.findByChannelAndUser(channel, user)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Membership not found"));

                // Prevent creator from leaving their own channel
                if (channel.getCreator().getId().equals(currentUserId)) {
                        throw new ResponseStatusException(BAD_REQUEST, "Channel creator cannot leave the channel");
                }

                channelMembershipRepository.delete(membership);
        }

        @Override
        public List<ChannelResponseDto> listJoinedChannels() {
                UUID currentUserId = SecurityUtil.getCurrentUserId()
                                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

                User user = userRepository.findById(currentUserId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

                List<ChannelMembership> memberships = channelMembershipRepository.findAllByUser(user);

                return memberships.stream()
                                .map(m -> modelMapper.map(m.getChannel(), ChannelResponseDto.class))
                                .collect(Collectors.toList());
        }

        @Override
        public ChannelResponseDto getChannel(UUID channelId) {
                UUID currentUserId = SecurityUtil.getCurrentUserId()
                                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

                Channel channel = channelRepository.findById(channelId)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Channel not found"));

                // Check if user is a member of the channel
                if (!isChannelMember(channelId, currentUserId)) {
                        throw new ResponseStatusException(FORBIDDEN,
                                        "Access denied. You are not a member of this channel.");
                }

                return modelMapper.map(channel, ChannelResponseDto.class);
        }

        @Override
        public ChannelResponseDto getChannelByCode(String channelCode) {
                Channel channel = channelRepository.findByChannelCode(channelCode)
                                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Channel not found"));

                return modelMapper.map(channel, ChannelResponseDto.class);
        }

        @Override
        public boolean isChannelMemberByCode(String channelCode) {
                UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
                if (currentUserId == null) {
                        return false;
                }

                Channel channel = channelRepository.findByChannelCode(channelCode).orElse(null);
                if (channel == null) {
                        return false;
                }

                return channelMembershipRepository.existsByChannelIdAndUserId(channel.getId(), currentUserId);
        }

        @Override
        public boolean isChannelModeratorByCode(String channelCode) {
                UUID currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
                if (currentUserId == null) {
                        return false;
                }

                Channel channel = channelRepository.findByChannelCode(channelCode).orElse(null);
                if (channel == null) {
                        return false;
                }

                return channelMembershipRepository.findByChannelIdAndUserId(channel.getId(), currentUserId)
                                .map(membership -> membership.getRole() == ChannelRole.MODERATOR)
                                .orElse(false);
        }

        // Helper methods
        private boolean isChannelMember(UUID channelId, UUID userId) {
                return channelMembershipRepository.existsByChannelIdAndUserId(channelId, userId);
        }

        private String generateUniqueChannelCode() {
                String channelCode;
                do {
                        channelCode = UUID.randomUUID().toString().substring(0, 8);
                } while (channelRepository.existsByChannelCode(channelCode));
                return channelCode;
        }
}