package com.crowdquery.crowdquery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.crowdquery.crowdquery.security.JwtAuthenticationFilter;
import com.crowdquery.crowdquery.security.OAuth2AuthenticationSuccessHandler;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

        private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/login.html", "/css/**", "/js/**", "/images/**")
                                                .permitAll()
                                                .requestMatchers("/auth/**", "/oauth2/**", "/error").permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth -> oauth
                                                .loginPage("/login.html")
                                                .successHandler(oAuth2AuthenticationSuccessHandler))
                                .logout(logout -> logout
                                                .logoutUrl("/auth/logout")
                                                .logoutSuccessUrl("/login.html")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID", "accessToken", "refreshToken")
                                                .permitAll())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.sendRedirect("/login.html");
                                                }))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
