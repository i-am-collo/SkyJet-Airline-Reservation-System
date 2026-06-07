package com.skyjet.backend.controller;

import com.skyjet.backend.dto.AuthResponse;
import com.skyjet.backend.dto.ChangePasswordRequest;
import com.skyjet.backend.dto.LoginRequest;
import com.skyjet.backend.dto.RegisterRequest;
import com.skyjet.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request,
                              HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request,
                                 HttpServletRequest httpRequest) {
        return authService.register(request, httpRequest);
    }

    /**
     * Returns the current user's profile from their JWT token.
     * Useful for session refresh and frontend state sync.
     */
    @GetMapping("/me")
    public AuthResponse getCurrentUser(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }

    /**
     * Change password for the currently authenticated user.
     */
    @PostMapping("/change-password")
    public Map<String, String> changePassword(Authentication authentication,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return Map.of("message", "Password changed successfully");
    }
}
