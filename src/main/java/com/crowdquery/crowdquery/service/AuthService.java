package com.crowdquery.crowdquery.service;

import com.crowdquery.crowdquery.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface AuthService {
    void logout(HttpServletResponse response);

    Optional<User> getCurrentUser(HttpServletRequest request);

    boolean refreshToken(HttpServletRequest request, HttpServletResponse response);
}
