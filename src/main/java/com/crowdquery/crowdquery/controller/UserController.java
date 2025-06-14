package com.crowdquery.crowdquery.controller;

import java.util.UUID;

import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.crowdquery.crowdquery.dto.UserDto;
import com.crowdquery.crowdquery.dto.UsernameSetupRequest;
import com.crowdquery.crowdquery.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/setup-username")
    public ResponseEntity<UserDto> setupUsername(@AuthenticationPrincipal(expression = "id") UUID userId,
            @Valid @RequestBody UsernameSetupRequest request) throws BadRequestException {
        UserDto userDto = userService.setupAnonymousUsername(userId, request);
        return ResponseEntity.ok(userDto);
    }
}
