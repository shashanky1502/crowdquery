package com.crowdquery.crowdquery.service.impl;

import com.crowdquery.crowdquery.model.User;
import com.crowdquery.crowdquery.repository.UserRepository;
import com.crowdquery.crowdquery.security.JwtTokenProvider;
import com.crowdquery.crowdquery.service.AuthService;
import com.crowdquery.crowdquery.util.SecurityUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void logout(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    @Override
    public Optional<User> getCurrentUser(HttpServletRequest request) {
        return SecurityUtil.getCurrentUserId()
                .flatMap(userRepository::findById);
    }

    @Override
    public boolean refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return false;
        }
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (jwtTokenProvider.getAccessTokenExpirationMs() / 1000));
        response.addCookie(accessTokenCookie);

        return true;
    }
}