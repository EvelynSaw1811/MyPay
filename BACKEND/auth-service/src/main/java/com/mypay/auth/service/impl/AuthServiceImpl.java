package com.mypay.auth.service.impl;

import com.mypay.auth.config.JwtUtil;
import com.mypay.auth.dto.*;
import com.mypay.auth.entity.RevokedToken;
import com.mypay.auth.entity.User;
import com.mypay.auth.entity.UserCredential;
import com.mypay.auth.entity.UserCredentialLegacy;
import com.mypay.auth.entity.UserLegacy;
import com.mypay.auth.mapper.UserMapper;
import com.mypay.auth.repository.RevokedTokenRepository;
import com.mypay.auth.repository.UserCredentialLegacyRepository;
import com.mypay.auth.repository.UserCredentialRepository;
import com.mypay.auth.repository.UserLegacyRepository;
import com.mypay.auth.repository.UserRepository;
import com.mypay.auth.messaging.UserEventPublisher;
import com.mypay.auth.service.AuthService;
import com.mypay.auth.service.InvitationCodeService;
import com.mypay.common.exception.BadRequestException;
import com.mypay.common.exception.DuplicateResourceException;
import com.mypay.common.exception.ForbiddenException;
import com.mypay.common.exception.ResourceNotFoundException;
import com.mypay.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final UserLegacyRepository userLegacyRepository;
    private final UserCredentialLegacyRepository userCredentialLegacyRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;
    private final InvitationCodeService invitationCodeService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUserEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setUserInvitationCode(invitationCodeService.generateUniqueInvitationCode());
        user = userRepository.save(user);

        UserCredential credential = UserCredential.builder()
                .userCredentialUserId(user.getUserId())
                .userCredentialPwdHash(passwordEncoder.encode(request.getPassword()))
                .build();
        credentialRepository.save(credential);

        final String userId = user.getUserId();
        userEventPublisher.publishUserRegistered(userId, user.getUserNickname(), request.getWalletCurrencies());

        String accessToken = jwtUtil.generateAccessToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        return authResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUserEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        ensureActive(user);

        UserCredential credential = credentialRepository.findByUserCredentialUserId(user.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getUserCredentialPwdHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Record last login timestamp
        user.setUserLastLogin(LocalDateTime.now());
        user = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        return authResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        String token = request.getRefreshToken();
        if (!jwtUtil.isValid(token)) {
            return; // already expired — nothing to blacklist
        }
        String hash = jwtUtil.hashToken(token);
        if (!revokedTokenRepository.existsByRevokedTokenHash(hash)) {
            String userId = jwtUtil.extractUserId(token);
            revokedTokenRepository.save(RevokedToken.builder()
                    .revokedTokenHash(hash)
                    .revokedTokenUserId(userId)
                    .revokedTokenExpireDateTime(jwtUtil.extractExpiry(token))
                    .build());
        }
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtUtil.isValid(request.getRefreshToken())) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }
        String hash = jwtUtil.hashToken(request.getRefreshToken());
        if (revokedTokenRepository.existsByRevokedTokenHash(hash)) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        String userId = jwtUtil.extractUserId(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        ensureActive(user);

        String newAccessToken = jwtUtil.generateAccessToken(userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId);

        return authResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest request, String authUserId) {
        if (userId == null || userId.isBlank()) {
            throw new ResourceNotFoundException("User not found");
        }
        if (authUserId == null || !authUserId.equals(userId)) {
            throw new ForbiddenException("You can only update your own profile");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Email uniqueness check — only when email is actually changing.
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(user.getUserEmail())
                    && userRepository.existsByUserEmail(newEmail)) {
                throw new DuplicateResourceException("Email already in use: " + newEmail);
            }
        }

        userMapper.applyUpdate(user, request);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse resolveUser(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ResourceNotFoundException("User not found");
        }
        String value = identifier.trim();
        User user = userRepository.findById(value)
                .or(() -> userRepository.findByUserEmail(value))
                .or(() -> userRepository.findByUserPhone(value))
                .or(() -> userRepository.findByUserNickname(value))
                .or(() -> userRepository.findByUserInvitationCode(value))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + identifier));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request, String authUserId) {
        if (userId == null || !userId.equals(authUserId)) {
            throw new ForbiddenException("You can only change your own password");
        }

        UserCredential credential = credentialRepository.findByUserCredentialUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), credential.getUserCredentialPwdHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), credential.getUserCredentialPwdHash())) {
            throw new BadRequestException("New password must differ from the current password");
        }

        credential.setUserCredentialPwdHash(passwordEncoder.encode(request.getNewPassword()));
        credentialRepository.save(credential);
        log.info("[Auth] Password changed for user {}", userId);
    }

    @Override
    @Transactional
    public void deleteAccount(String userId, DeleteAccountRequest request, String authUserId) {
        if (userId == null || !userId.equals(authUserId)) {
            throw new ForbiddenException("You can only delete your own account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        UserCredential credential = credentialRepository.findByUserCredentialUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User credentials not found"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getUserCredentialPwdHash())) {
            throw new UnauthorizedException("Password is incorrect");
        }

        LocalDateTime now = LocalDateTime.now();

        // Archive user record
        userLegacyRepository.save(UserLegacy.builder()
                .userId(user.getUserId())
                .userEmail(user.getUserEmail())
                .userPhone(user.getUserPhone())
                .userFirstName(user.getUserFirstName())
                .userLastName(user.getUserLastName())
                .userNickname(user.getUserNickname())
                .userInvitationCode(user.getUserInvitationCode())
                .userStatus(user.getUserStatus())
                .userLastLogin(user.getUserLastLogin())
                .userCreated(user.getUserCreated())
                .userUpdated(user.getUserUpdated())
                .userDeleted(now)
                .build());

        // Archive credentials
        userCredentialLegacyRepository.save(UserCredentialLegacy.builder()
                .userCredentialId(credential.getUserCredentialId())
                .userCredentialUserId(credential.getUserCredentialUserId())
                .userCredentialPwdHash(credential.getUserCredentialPwdHash())
                .userCredentialCreated(credential.getUserCredentialCreated())
                .userCredentialDeleted(now)
                .build());

        // Delete from live tables (credential first to avoid FK issues)
        credentialRepository.delete(credential);
        userRepository.delete(user);

        log.info("[Auth] Account deleted and archived for user {}", userId);
    }

    private void ensureActive(User user) {
        if (!"ACTIVE".equalsIgnoreCase(user.getUserStatus())) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    private AuthResponse authResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .email(user.getUserEmail())
                .firstName(user.getUserFirstName())
                .lastName(user.getUserLastName())
                .userNickname(user.getUserNickname())
                .invitationCode(user.getUserInvitationCode())
                .name(displayName(user))
                .build();
    }

    private String displayName(User user) {
        if (user.getUserNickname() != null && !user.getUserNickname().isBlank()) {
            return user.getUserNickname();
        }
        return (user.getUserFirstName() + " " + user.getUserLastName()).trim();
    }
}
