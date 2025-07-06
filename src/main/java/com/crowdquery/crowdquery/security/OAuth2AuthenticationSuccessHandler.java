package com.crowdquery.crowdquery.security;

import com.crowdquery.crowdquery.enums.Role;
import com.crowdquery.crowdquery.model.User;
import com.crowdquery.crowdquery.repository.UserRepository;
import com.crowdquery.crowdquery.service.RandomUserService;

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
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RandomUserService randomUserService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String googleId = oauthUser.getAttribute("sub");

        String email = oauthUser.getAttribute("email");

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    Map<String, String> userInfo = randomUserService.fetchUsernameAndAvatar()
                            .orElseGet(() -> {
                                String defaultUsername = "anon" + UUID.randomUUID().toString().substring(0, 8);
                                String defaultAvatar = "https://www.redditstatic.com/avatars/defaults/v2/avatar_default_5.png";
                                return Map.of("username", defaultUsername, "avatarUrl", defaultAvatar);
                            });

                    String baseUsername = userInfo.get("username");
                    String avatarUrl = userInfo.get("avatarUrl");
                    String finalUsername = baseUsername;
                    int counter = 1;
                    while (userRepository.existsByAnonymousUsername(finalUsername)) {
                        finalUsername = baseUsername + counter++;
                    }

                    User newUser = User.builder()
                            .googleId(googleId)
                            .email(email)
                            .anonymousUsername(finalUsername)
                            .avatarUrl(avatarUrl)
                            .role(Role.USER)
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
        refreshTokenCookie.setAttribute("SameSite", "Strict");
        // @Refactor: Uncomment and set secure and domain in production
        // refreshTokenCookie.setSecure(true); // Ensure this is set to true in production
        // refreshTokenCookie.setDomain("localhost"); // Set your domain here


        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        response.sendRedirect("https://crowdquery-production.up.railway.app/dashboard.html");
    }
}
