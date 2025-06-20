package com.crowdquery.crowdquery.service;

import org.springframework.web.reactive.function.client.WebClient;

import com.crowdquery.crowdquery.dto.RandomUserResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

// This service is used to generate random username and avatar for anonymous users
@Service
public class RandomUserService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://randomuser.me")
            .build();

    public Optional<Map<String, String>> fetchUsernameAndAvatar() {
        try {
            return webClient.get()
                    .uri("/api")
                    .retrieve()
                    .bodyToMono(RandomUserResponse.class)
                    .blockOptional()
                    .flatMap(res -> res.getResults().stream().findFirst())
                    .map(user -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("username", user.getLogin().getUsername());
                        map.put("avatarUrl", user.getPicture().getLarge());
                        return map;
                    });
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
