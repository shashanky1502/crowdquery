package com.crowdquery.crowdquery.service.impl;

import com.crowdquery.crowdquery.service.ImageUploadService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
public class ImgBBUploadService implements ImageUploadService {

    private final RestTemplate restTemplate;

    @Value("${app.imgbb.api-key}")
    private String imgbbApiKey;

    @Override
    public String uploadImage(MultipartFile image) {
        try {
            // Convert image to Base64
            byte[] imageBytes = image.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Prepare form data
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("key", imgbbApiKey);
            body.add("image", base64Image);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.imgbb.com/1/upload",
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                return root.path("data").path("url").asText(); // or display_url
            } else {
                throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ImgBB upload failed");
            }

        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ImgBB upload error: " + e.getMessage());
        }
    }
}
