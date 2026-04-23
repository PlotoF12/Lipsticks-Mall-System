package com.example.lipsticks.auth.controller;

import com.example.lipsticks.auth.dto.ChangePasswordRequest;
import com.example.lipsticks.auth.dto.LoginRequest;
import com.example.lipsticks.auth.dto.LoginResponse;
import com.example.lipsticks.auth.dto.RegisterRequest;
import com.example.lipsticks.auth.dto.UpdateProfileRequest;
import com.example.lipsticks.auth.dto.UserMeResponse;
import com.example.lipsticks.auth.service.AuthService;
import com.example.lipsticks.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ApiResponse.ok(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ApiResponse.ok(authService.login(request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ApiResponse<UserMeResponse> me(Authentication authentication) {
        try {
            return ApiResponse.ok(authService.getMe(authentication.getName()));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PutMapping("/me")
    public ApiResponse<Void> updateMe(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        try {
            authService.updateProfile(authentication.getName(), request);
            return ApiResponse.ok(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/me/password")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        try {
            authService.changePassword(authentication.getName(), request);
            return ApiResponse.ok(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
