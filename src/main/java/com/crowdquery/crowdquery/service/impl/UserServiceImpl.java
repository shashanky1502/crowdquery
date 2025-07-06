package com.crowdquery.crowdquery.service.impl;

import com.crowdquery.crowdquery.dto.CurrentUserDto;
import com.crowdquery.crowdquery.dto.UserDto;
import com.crowdquery.crowdquery.dto.UserProfileUpdateDto;
import com.crowdquery.crowdquery.model.User;
import com.crowdquery.crowdquery.repository.UserRepository;
import com.crowdquery.crowdquery.service.UserService;
import com.crowdquery.crowdquery.service.ImageUploadService;
import com.crowdquery.crowdquery.util.IdEncoder;
import com.crowdquery.crowdquery.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;
    private final IdEncoder idEncoder;

    @Override
    public CurrentUserDto getMyProfile() {
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        return mapToCurrentUserDto(user);
    }

    @Override
    public UserDto getUserProfile(String username) {
        User user = userRepository.findByAnonymousUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        return mapToUserDto(user);
    }

    @Override
    @Transactional
    public CurrentUserDto updateUsername(UserProfileUpdateDto request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        // Check if username is different and available
        if (!request.getAnonymousUsername().equals(user.getAnonymousUsername())) {
            if (userRepository.existsByAnonymousUsername(request.getAnonymousUsername())) {
                throw new ResponseStatusException(BAD_REQUEST, "Username is already taken.");
            }
            user.setAnonymousUsername(request.getAnonymousUsername());
        }

        userRepository.save(user);
        return mapToCurrentUserDto(user);
    }

    @Override
    @Transactional
    public CurrentUserDto updateAvatar(MultipartFile avatarFile) {
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        try {
            String avatarUrl = imageUploadService.uploadImage(avatarFile);
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            return mapToCurrentUserDto(user);
        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to upload avatar: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CurrentUserDto updateAvatarUrl(String avatarUrl) {
        UUID currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not authenticated"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return mapToCurrentUserDto(user);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByAnonymousUsername(username);
    }

    private CurrentUserDto mapToCurrentUserDto(User user) {
        CurrentUserDto dto = new CurrentUserDto();
        dto.setId(idEncoder.encode(user.getId()));
        dto.setEmail(user.getEmail());
        dto.setAnonymousUsername(user.getAnonymousUsername());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole());
        return dto;
    }

    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(idEncoder.encode(user.getId()));
        dto.setAnonymousUsername(user.getAnonymousUsername());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }
}