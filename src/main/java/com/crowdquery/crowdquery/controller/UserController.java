package com.crowdquery.crowdquery.controller;

import com.crowdquery.crowdquery.dto.AvatarUpdateDto;
import com.crowdquery.crowdquery.dto.CurrentUserDto;
import com.crowdquery.crowdquery.dto.UserDto;
import com.crowdquery.crowdquery.dto.UserProfileUpdateDto;
import com.crowdquery.crowdquery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get my profile (includes email)
    @GetMapping("/me")
    public ResponseEntity<CurrentUserDto> getMyProfile() {
        CurrentUserDto userDto = userService.getMyProfile();
        return ResponseEntity.ok(userDto);
    }

    // Get public profile by username (no email)
    @GetMapping("/{username}")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable String username) {
        UserDto userDto = userService.getUserProfile(username);
        return ResponseEntity.ok(userDto);
    }

    // Update username
    @PutMapping("/me/username")
    public ResponseEntity<CurrentUserDto> updateUsername(@Valid @RequestBody UserProfileUpdateDto request) {
        CurrentUserDto userDto = userService.updateUsername(request);
        return ResponseEntity.ok(userDto);
    }

    // Upload avatar image
    @PostMapping("/me/avatar")
    public ResponseEntity<CurrentUserDto> uploadAvatar(@RequestParam("avatar") MultipartFile avatarFile) {
        CurrentUserDto userDto = userService.updateAvatar(avatarFile);
        return ResponseEntity.ok(userDto);
    }

    // Update avatar URL
    @PutMapping("/me/avatar")
    public ResponseEntity<CurrentUserDto> updateAvatarUrl(@Valid @RequestBody AvatarUpdateDto request) {
        CurrentUserDto userDto = userService.updateAvatarUrl(request.getAvatarUrl());
        return ResponseEntity.ok(userDto);
    }

    // Check username availability
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(isAvailable);
    }
}