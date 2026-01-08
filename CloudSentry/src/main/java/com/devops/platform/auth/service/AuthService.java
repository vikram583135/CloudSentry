package com.devops.platform.auth.service;

import com.devops.platform.auth.dto.AuthResponse;
import com.devops.platform.auth.dto.LoginRequest;
import com.devops.platform.auth.dto.RefreshTokenRequest;
import com.devops.platform.auth.dto.RegisterRequest;
import com.devops.platform.auth.model.RefreshToken;
import com.devops.platform.auth.model.Role;
import com.devops.platform.auth.model.User;
import com.devops.platform.auth.repository.RefreshTokenRepository;
import com.devops.platform.auth.repository.UserRepository;
import com.devops.platform.auth.security.JwtService;
import com.devops.platform.auth.security.UserPrincipal;
import com.devops.platform.common.exception.BadRequestException;
import com.devops.platform.common.exception.ResourceNotFoundException;
import com.devops.platform.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authentication Service handling login, registration, and token refresh.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticate user and return tokens.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.debug("Attempting login for user: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        String accessToken = jwtService.generateToken(userPrincipal);
        String refreshToken = createRefreshToken(userPrincipal);

        log.info("User logged in successfully: {}", request.getEmail());
        
        return buildAuthResponse(userPrincipal, accessToken, refreshToken);
    }

    /**
     * Register a new user.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Attempting registration for user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // Default to DEVELOPER role if not specified
        Set<Role> roles = request.getRoles() != null && !request.getRoles().isEmpty() 
                ? request.getRoles() 
                : Set.of(Role.DEVELOPER);

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .enabled(true)
                .roles(roles)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", request.getEmail());

        UserPrincipal userPrincipal = new UserPrincipal(user);
        String accessToken = jwtService.generateToken(userPrincipal);
        String refreshToken = createRefreshToken(userPrincipal);

        return buildAuthResponse(userPrincipal, accessToken, refreshToken);
    }

    /**
     * Refresh access token using refresh token.
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Attempting token refresh");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        UserPrincipal userPrincipal = new UserPrincipal(user);

        String newAccessToken = jwtService.generateToken(userPrincipal);
        
        // Rotate refresh token for security
        refreshTokenRepository.delete(refreshToken);
        String newRefreshToken = createRefreshToken(userPrincipal);

        log.debug("Token refreshed successfully for user: {}", user.getEmail());
        
        return buildAuthResponse(userPrincipal, newAccessToken, newRefreshToken);
    }

    /**
     * Logout user by invalidating refresh token.
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    log.info("User logged out: {}", token.getUser().getEmail());
                    refreshTokenRepository.delete(token);
                });
    }

    /**
     * Logout user from all sessions.
     */
    @Transactional
    public void logoutAll(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        refreshTokenRepository.deleteByUser(user);
        log.info("User logged out from all sessions: {}", user.getEmail());
    }

    private String createRefreshToken(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusMillis(jwtService.getRefreshExpiration());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private AuthResponse buildAuthResponse(UserPrincipal userPrincipal, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration() / 1000) // Convert to seconds
                .user(AuthResponse.UserInfo.builder()
                        .id(userPrincipal.getId())
                        .name(userPrincipal.getName())
                        .email(userPrincipal.getEmail())
                        .roles(userPrincipal.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .map(role -> role.replace("ROLE_", ""))
                                .collect(Collectors.toList()))
                        .build())
                .build();
    }
}
