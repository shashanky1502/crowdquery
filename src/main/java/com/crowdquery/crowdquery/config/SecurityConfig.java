package com.crowdquery.crowdquery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.crowdquery.crowdquery.security.OAuth2AuthenticationSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**","/login").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2AuthenticationSuccessHandler));

        return http.build();
    }
}