package itk.student.task.manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import itk.student.task.manager.dto.common.PagedResponse;
import itk.student.task.manager.dto.request.UpdateUserRequest;
import itk.student.task.manager.dto.response.TaskResponse;
import itk.student.task.manager.dto.response.UserProfileResponse;
import itk.student.task.manager.dto.response.UserResponse;
import itk.student.task.manager.enums.TaskStatus;
import itk.student.task.manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Управление пользователями и профилем.
 * Список и удаление — только для ADMIN.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Пользователи и профиль")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Список пользователей", description = "Только ADMIN. Поддерживает search и pagination")
    public PagedResponse<UserResponse> listUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "email", direction = Sort.Direction.ASC) Pageable pageable) {
        return userService.listUsers(search, pageable);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Пользователь по ID")
    public UserResponse getUser(@PathVariable UUID userId) {
        return userService.getUser(userId);
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Обновление пользователя", description = "Self или ADMIN")
    public UserResponse updateUser(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete пользователя", description = "Только ADMIN")
    public void deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
    }

    @GetMapping("/{userId}/tasks")
    @Operation(summary = "Задачи пользователя", description = "Фильтр по status, pagination")
    public PagedResponse<TaskResponse> getUserTasks(
            @PathVariable UUID userId,
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return userService.getUserTasks(userId, status, pageable);
    }

    @GetMapping("/me/profile")
    @Operation(summary = "Расширенный профиль текущего пользователя")
    public UserProfileResponse myProfile() {
        return userService.getMyProfile();
    }
}
