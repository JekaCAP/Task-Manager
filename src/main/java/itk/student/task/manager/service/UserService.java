package itk.student.task.manager.service;

import itk.student.task.manager.dto.common.PagedResponse;
import itk.student.task.manager.dto.request.UpdateUserRequest;
import itk.student.task.manager.dto.response.TaskResponse;
import itk.student.task.manager.dto.response.UserProfileResponse;
import itk.student.task.manager.dto.response.UserResponse;
import itk.student.task.manager.entity.User;
import itk.student.task.manager.enums.TaskStatus;
import itk.student.task.manager.exception.ForbiddenException;
import itk.student.task.manager.exception.ResourceNotFoundException;
import itk.student.task.manager.mapper.PageMapper;
import itk.student.task.manager.mapper.TaskMapper;
import itk.student.task.manager.mapper.UserMapper;
import itk.student.task.manager.repository.ProjectRepository;
import itk.student.task.manager.repository.TaskRepository;
import itk.student.task.manager.repository.UserRepository;
import itk.student.task.manager.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final PageMapper pageMapper;

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> listUsers(String search, Pageable pageable) {
        if (!SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Only admin can list users");
        }
        Page<User> page = (search == null || search.isBlank())
                ? userRepository.findByDeletedAtIsNull(pageable)
                : userRepository.findByDeletedAtIsNullAndEmailContainingIgnoreCase(search.trim(), pageable);
        return pageMapper.toPagedResponse(page, userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        User user = findActiveUser(userId);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.currentUserId().equals(userId)) {
            throw new ForbiddenException("You can update only your own profile");
        }
        User user = findActiveUser(userId);
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID userId) {
        if (!SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Only admin can delete users");
        }
        User user = findActiveUser(userId);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getUserTasks(UUID userId, TaskStatus status, Pageable pageable) {
        findActiveUser(userId);
        Page<itk.student.task.manager.entity.Task> page = status == null
                ? taskRepository.findByAssigneeIdAndDeletedAtIsNull(userId, pageable)
                : taskRepository.findByAssigneeIdAndStatusAndDeletedAtIsNull(userId, status, pageable);
        return pageMapper.toPagedResponse(page, taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        UUID userId = SecurityUtils.currentUserId();
        User user = findActiveUser(userId);
        return new UserProfileResponse(
                userMapper.toResponse(user),
                taskRepository.countByAssigneeIdAndDeletedAtIsNull(userId),
                projectRepository.countByOwnerId(userId),
                user.getCreatedAt()
        );
    }

    private User findActiveUser(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
