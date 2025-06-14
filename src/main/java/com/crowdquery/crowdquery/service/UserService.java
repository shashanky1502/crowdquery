package com.crowdquery.crowdquery.service;

import com.crowdquery.crowdquery.dto.UserDto;
import com.crowdquery.crowdquery.dto.UsernameSetupRequest;

import java.util.UUID;

import org.apache.coyote.BadRequestException;

public interface UserService {

    UserDto setupAnonymousUsername(UUID userId, UsernameSetupRequest request) throws BadRequestException;

}
