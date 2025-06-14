package com.crowdquery.crowdquery.security;

import com.crowdquery.crowdquery.enums.Role;
import com.crowdquery.crowdquery.model.User;
import com.crowdquery.crowdquery.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String googleId = oauthUser.getAttribute("sub");

        String email = oauthUser.getAttribute("email");

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .googleId(googleId)
                            .email(email)
                            .role(Role.USER)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return userRepository.saveAndFlush(newUser);
                });

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (jwtTokenProvider.getAccessTokenExpirationMs() / 1000));

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpirationMs() / 1000));

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        response.sendRedirect("http://localhost:8080/dashboard.html");
    }
}
