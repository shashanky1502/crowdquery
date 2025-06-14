package com.crowdquery.crowdquery.service.impl;

import com.crowdquery.crowdquery.dto.UserDto;
import com.crowdquery.crowdquery.dto.UsernameSetupRequest;
import com.crowdquery.crowdquery.repository.UserRepository;
import com.crowdquery.crowdquery.service.UserService;
import com.crowdquery.crowdquery.model.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDto setupAnonymousUsername(UUID userId, UsernameSetupRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        if (user.getLastUsernameChangeAt() != null &&
                user.getLastUsernameChangeAt().isAfter(LocalDateTime.now().minusDays(1))) {
            throw new ResponseStatusException(BAD_REQUEST, "You can only change your username once every 24 hours.");
        }

        if (userRepository.existsByAnonymousUsername(request.getAnonymousUsername())) {
            throw new ResponseStatusException(BAD_REQUEST, "Username is already taken.");
        }

        user.setAnonymousUsername(request.getAnonymousUsername());
        user.setLastUsernameChangeAt(LocalDateTime.now());

        userRepository.save(user);

        return modelMapper.map(user, UserDto.class);
    }
}
