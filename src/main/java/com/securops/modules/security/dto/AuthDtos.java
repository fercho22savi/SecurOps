package com.securops.modules.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

public class AuthDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequestDto {
        private String username;
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponseDto {
        private String token;
        @Builder.Default
        private String tokenType = "Bearer";
        private String username;
        private String fullName;
        private Set<String> roles;
        private long expiresInMs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequestDto {
        private String username;
        private String password;
        private String fullName;
        private String email;
        private Set<String> roles;
    }
}
