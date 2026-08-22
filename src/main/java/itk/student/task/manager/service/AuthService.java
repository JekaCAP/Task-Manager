package itk.student.task.manager.service;

import itk.student.task.manager.dto.request.LoginRequest;
import itk.student.task.manager.dto.request.RefreshTokenRequest;
import itk.student.task.manager.dto.request.RegisterRequest;
import itk.student.task.manager.dto.response.AuthResponse;
import itk.student.task.manager.dto.response.TokenResponse;
import itk.student.task.manager.dto.response.UserResponse;
import itk.student.task.manager.entity.RefreshToken;
import itk.student.task.manager.entity.User;
import itk.student.task.manager.enums.GlobalRole;
import itk.student.task.manager.exception.ConflictException;
import itk.student.task.manager.exception.UnauthorizedException;
import itk.student.task.manager.mapper.UserMapper;
import itk.student.task.manager.repository.RefreshTokenRepository;
import itk.student.task.manager.repository.UserRepository;
import itk.student.task.manager.security.JwtService;
import itk.student.task.manager.security.SecurityUtils;
import itk.student.task.manager.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.email())) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setGlobalRole(GlobalRole.USER);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(request.email().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        return buildAuthResponse(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid or expired"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(stored.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getGlobalRole().name());
        RefreshToken newRefresh = createRefreshToken(user.getId());
        return new TokenResponse(accessToken, newRefresh.getToken());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        UserPrincipal principal = SecurityUtils.currentUser();
        User user = userRepository.findByIdAndDeletedAtIsNull(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return userMapper.toResponse(user);
    }

    public void forgotPassword(String email) {
        // Anti-enumeration: always accept request without side effects in sandbox.
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getGlobalRole().name());
        RefreshToken refreshToken = createRefreshToken(user.getId());
        return new AuthResponse(accessToken, refreshToken.getToken(), userMapper.toResponse(user));
    }

    private RefreshToken createRefreshToken(java.util.UUID userId) {
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setToken(jwtService.generateRefreshTokenValue());
        token.setExpiresAt(jwtService.refreshTokenExpiry());
        token.setRevoked(false);
        return refreshTokenRepository.save(token);
    }
}
