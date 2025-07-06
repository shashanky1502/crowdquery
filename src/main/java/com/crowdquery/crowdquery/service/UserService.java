package com.crowdquery.crowdquery.service;

import com.crowdquery.crowdquery.dto.CurrentUserDto;
import com.crowdquery.crowdquery.dto.UserDto;
import com.crowdquery.crowdquery.dto.UserProfileUpdateDto;

import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    // Profile management
    CurrentUserDto getMyProfile(); // Returns CurrentUserDto with email

    UserDto getUserProfile(String username); // Returns UserDto without email

    CurrentUserDto updateUsername(UserProfileUpdateDto request);

    // Avatar management
    CurrentUserDto updateAvatar(MultipartFile avatarFile);

    CurrentUserDto updateAvatarUrl(String avatarUrl);

    // Utility methods
    boolean isUsernameAvailable(String username);
}
