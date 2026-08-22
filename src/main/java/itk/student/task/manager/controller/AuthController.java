package itk.student.task.manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import itk.student.task.manager.dto.request.ForgotPasswordRequest;
import itk.student.task.manager.dto.request.LoginRequest;
import itk.student.task.manager.dto.request.RefreshTokenRequest;
import itk.student.task.manager.dto.request.RegisterRequest;
import itk.student.task.manager.dto.response.AuthResponse;
import itk.student.task.manager.dto.response.TokenResponse;
import itk.student.task.manager.dto.response.UserResponse;
import itk.student.task.manager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Аутентификация и управление JWT-токенами.
 * Публичные эндпоинты для регистрации, входа и обновления access token.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Регистрация, login, refresh, logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация пользователя", description = "Создаёт аккаунт и возвращает JWT access/refresh tokens")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Возвращает JWT tokens при корректных email/password")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление access token", description = "По refresh token выдаёт новую пару access/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Logout", description = "Отзывает refresh token")
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Текущий пользователь", description = "Возвращает профиль по JWT access token")
    public UserResponse me() {
        return authService.me();
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Запрос сброса пароля", description = "Sandbox: всегда 202 Accepted (anti-enumeration)")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
    }
}