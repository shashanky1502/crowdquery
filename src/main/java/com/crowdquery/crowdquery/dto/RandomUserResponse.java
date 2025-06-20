package com.crowdquery.crowdquery.dto;

import java.util.List;

import lombok.Data;

@Data
public class RandomUserResponse {
    private List<RandomUser> results;

    @Data
    public static class RandomUser {
        private Login login;
        private Picture picture;

        @Data
        public static class Login {
            private String username;
        }

        @Data
        public static class Picture {
            private String large;
        }
    }
}