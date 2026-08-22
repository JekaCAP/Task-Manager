package itk.student.task.manager.service;

import itk.student.task.manager.dto.common.PagedResponse;
import itk.student.task.manager.dto.request.BulkUpdateTasksRequest;
import itk.student.task.manager.dto.request.CreateTaskRequest;
import itk.student.task.manager.dto.request.UpdateTaskAssigneeRequest;
import itk.student.task.manager.dto.request.UpdateTaskRequest;
import itk.student.task.manager.dto.request.UpdateTaskStatusRequest;
import itk.student.task.manager.dto.response.ActivityLogResponse;
import itk.student.task.manager.dto.response.BulkUpdateTasksResponse;
import itk.student.task.manager.dto.response.TaskDetailResponse;
import itk.student.task.manager.dto.response.TaskResponse;
import itk.student.task.manager.entity.ActivityLog;
import itk.student.task.manager.entity.Tag;
import itk.student.task.manager.entity.Task;
import itk.student.task.manager.entity.User;
import itk.student.task.manager.enums.ActivityAction;
import itk.student.task.manager.enums.TaskStatus;
import itk.student.task.manager.exception.BusinessRuleException;
import itk.student.task.manager.exception.ConflictException;
import itk.student.task.manager.exception.ResourceNotFoundException;
import itk.student.task.manager.mapper.ActivityLogMapper;
import itk.student.task.manager.mapper.PageMapper;
import itk.student.task.manager.mapper.TagMapper;
import itk.student.task.manager.mapper.TaskMapper;
import itk.student.task.manager.repository.ActivityLogRepository;
import itk.student.task.manager.repository.AttachmentRepository;
import itk.student.task.manager.repository.CommentRepository;
import itk.student.task.manager.repository.ProjectMemberRepository;
import itk.student.task.manager.repository.TagRepository;
import itk.student.task.manager.repository.TaskRepository;
import itk.student.task.manager.repository.TaskTagRepository;
import itk.student.task.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;
    private final TaskTagRepository taskTagRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskMapper taskMapper;
    private final TagMapper tagMapper;
    private final ActivityLogMapper activityLogMapper;
    private final PageMapper pageMapper;
    private final ProjectAccessService projectAccessService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> listTasks(UUID projectId,
                                                 TaskStatus status,
                                                 itk.student.task.manager.enums.TaskPriority priority,
                                                 UUID assigneeId,
                                                 UUID tag,
                                                 Instant dueBefore,
                                                 Instant dueAfter,
                                                 Pageable pageable) {
        if (projectId != null) {
            projectAccessService.requireReadAccess(projectId);
        }
        Specification<Task> spec = TaskSpecifications.filter(
                projectId, status, priority, assigneeId, tag, dueBefore, dueAfter, null);
        Page<Task> page = taskRepository.findAll(spec, pageable);
        return pageMapper.toPagedResponse(page, taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> searchTasks(String query, UUID projectId, Pageable pageable) {
        if (query == null || query.trim().length() < 2) {
            throw new BusinessRuleException("Search query must be at least 2 characters");
        }
        if (projectId != null) {
            projectAccessService.requireReadAccess(projectId);
        }
        Specification<Task> spec = TaskSpecifications.filter(
                projectId, null, null, null, null, null, null, query.trim());
        Page<Task> page = taskRepository.findAll(spec, pageable);
        return pageMapper.toPagedResponse(page, taskMapper::toResponse);
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        projectAccessService.requireWriteAccess(request.projectId());
        validateAssigneeInProject(request.projectId(), request.assigneeId());

        Task task = new Task();
        task.setProjectId(request.projectId());
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setAssigneeId(request.assigneeId());
        task.setStatus(TaskStatus.TODO);
        taskRepository.save(task);

        activityLogService.log(task.getId(), ActivityAction.CREATED, "Task created");
        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskDetailResponse getTask(UUID taskId) {
        Task task = findActiveTask(taskId);
        projectAccessService.requireReadAccess(task.getProjectId());

        List<Tag> tags = taskTagRepository.findByTaskId(taskId).stream()
                .map(taskTag -> tagRepository.findById(taskTag.getTagId()).orElseThrow())
                .toList();

        return new TaskDetailResponse(
                taskMapper.toResponse(task),
                tags.stream().map(tagMapper::toResponse).toList(),
                commentRepository.countByTaskId(taskId),
                attachmentRepository.countByTaskId(taskId)
        );
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request) {
        Task task = findActiveTask(taskId);
        projectAccessService.requireWriteAccess(task.getProjectId());
        assertVersion(task, request.version());

        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }

        try {
            Task saved = taskRepository.saveAndFlush(task);
            activityLogService.log(taskId, ActivityAction.UPDATED, "Task updated");
            return taskMapper.toResponse(saved);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictException("Resource was modified by another user");
        }
    }

    @Transactional
    public void deleteTask(UUID taskId, boolean hard) {
        Task task = findActiveTask(taskId);
        projectAccessService.requireWriteAccess(task.getProjectId());
        if (hard) {
            taskRepository.delete(task);
        } else {
            task.setDeletedAt(Instant.now());
            taskRepository.save(task);
            activityLogService.log(taskId, ActivityAction.DELETED, "Task soft deleted");
        }
    }

    @Transactional
    public TaskResponse updateStatus(UUID taskId, UpdateTaskStatusRequest request) {
        Task task = findActiveTask(taskId);
        projectAccessService.requireWriteAccess(task.getProjectId());
        assertVersion(task, request.version());
        TaskStatusValidator.validateTransition(task.getStatus(), request.status());
        task.setStatus(request.status());

        try {
            Task saved = taskRepository.saveAndFlush(task);
            activityLogService.log(taskId, ActivityAction.STATUS_CHANGED, "Status -> " + request.status());
            return taskMapper.toResponse(saved);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictException("Resource was modified by another user");
        }
    }

    @Transactional
    public TaskResponse updateAssignee(UUID taskId, UpdateTaskAssigneeRequest request) {
        Task task = findActiveTask(taskId);
        projectAccessService.requireWriteAccess(task.getProjectId());
        assertVersion(task, request.version());
        validateAssigneeInProject(task.getProjectId(), request.assigneeId());
        task.setAssigneeId(request.assigneeId());

        try {
            Task saved = taskRepository.saveAndFlush(task);
            activityLogService.log(taskId, ActivityAction.ASSIGNEE_CHANGED,
                    request.assigneeId() == null ? "Unassigned" : "Assignee changed");
            return taskMapper.toResponse(saved);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictException("Resource was modified by another user");
        }
    }

    @Transactional
    public BulkUpdateTasksResponse bulkUpdate(BulkUpdateTasksRequest request) {
        if (request.status() == null && request.priority() == null && request.assigneeId() == null) {
            throw new BusinessRuleException("At least one bulk field must be provided");
        }

        List<UUID> failed = new ArrayList<>();
        int updated = 0;

        for (UUID taskId : request.taskIds()) {
            try {
                Task task = findActiveTask(taskId);
                projectAccessService.requireWriteAccess(task.getProjectId());
                if (request.assigneeId() != null) {
                    validateAssigneeInProject(task.getProjectId(), request.assigneeId());
                    task.setAssigneeId(request.assigneeId());
                }
                if (request.priority() != null) {
                    task.setPriority(request.priority());
                }
                if (request.status() != null) {
                    TaskStatusValidator.validateTransition(task.getStatus(), request.status());
                    task.setStatus(request.status());
                }
                taskRepository.save(task);
                updated++;
            } catch (RuntimeException ex) {
                failed.add(taskId);
            }
        }

        return new BulkUpdateTasksResponse(updated, failed);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getHistory(UUID taskId) {
        Task task = findActiveTask(taskId);
        projectAccessService.requireReadAccess(task.getProjectId());
        return activityLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toActivityResponse)
                .toList();
    }

    private ActivityLogResponse toActivityResponse(ActivityLog log) {
        User actor = userRepository.findById(log.getActorId())
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found"));
        return activityLogMapper.toResponse(log, actor);
    }

    private Task findActiveTask(UUID taskId) {
        return taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private void assertVersion(Task task, Long version) {
        if (!task.getVersion().equals(version)) {
            throw new ConflictException("Resource was modified by another user");
        }
    }

    private void validateAssigneeInProject(UUID projectId, UUID assigneeId) {
        if (assigneeId == null) {
            return;
        }
        userRepository.findByIdAndDeletedAtIsNull(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assigneeId)) {
            throw new ResourceNotFoundException("Assignee is not a project member");
        }
    }
}
