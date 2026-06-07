package com.skyjet.backend.service;

import com.skyjet.backend.dto.AuthResponse;
import com.skyjet.backend.dto.ChangePasswordRequest;
import com.skyjet.backend.dto.LoginRequest;
import com.skyjet.backend.dto.RegisterRequest;
import com.skyjet.backend.entity.User;
import com.skyjet.backend.entity.enums.UserRole;
import com.skyjet.backend.exception.ApiException;
import com.skyjet.backend.repository.UserRepository;
import com.skyjet.backend.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuditService auditService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider,
                       AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String ip = extractIp(httpRequest);
        String ua = extractUserAgent(httpRequest);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (AuthenticationException ex) {
            auditService.recordLoginAttempt(email, false, "Invalid credentials", ip, ua);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        auditService.recordLoginAttempt(email, true, "Login successful", ip, ua);
        return authResponse(user);
    }

    /**
     * Overload for backward compatibility (without HttpServletRequest).
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        return login(request, null);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        // Split the single name field into first/last for the DB schema
        String[] names = splitName(request.getName().trim());

        User user = User.builder()
                .firstName(names[0])
                .lastName(names[1])
                .email(email)
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .build();

        User saved = userRepository.save(user);

        auditService.logAction(email, "CREATE", "USER", saved.getUserId(),
                "New user registered: " + saved.getFullName());

        return authResponse(saved);
    }

    /**
     * Overload for backward compatibility (without HttpServletRequest).
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        return register(request, null);
    }

    /**
     * Get the current user profile from email (for /auth/me endpoint).
     */
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return authResponse(user);
    }

    /**
     * Change password for authenticated user.
     */
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.logAction(email, "UPDATE", "USER", user.getUserId(),
                "Password changed");
    }

    private AuthResponse authResponse(User user) {
        return AuthResponse.builder()
                .token(jwtProvider.generateToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getExpirationSeconds())
                .userId(user.getUserId())
                .name(user.getFullName().trim())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Splits a single full name string into [firstName, lastName].
     */
    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{"", ""};
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        return new String[]{parts[0], parts[1]};
    }

    private String extractIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) return "unknown";
        return request.getHeader("User-Agent");
    }
}
