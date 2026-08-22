package itk.student.task.manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import itk.student.task.manager.dto.common.PagedResponse;
import itk.student.task.manager.dto.request.BulkUpdateTasksRequest;
import itk.student.task.manager.dto.request.CreateCommentRequest;
import itk.student.task.manager.dto.request.CreateTaskRequest;
import itk.student.task.manager.dto.request.UpdateTaskAssigneeRequest;
import itk.student.task.manager.dto.request.UpdateTaskRequest;
import itk.student.task.manager.dto.request.UpdateTaskStatusRequest;
import itk.student.task.manager.dto.response.ActivityLogResponse;
import itk.student.task.manager.dto.response.AttachmentResponse;
import itk.student.task.manager.dto.response.BulkUpdateTasksResponse;
import itk.student.task.manager.dto.response.CommentResponse;
import itk.student.task.manager.dto.response.TaskDetailResponse;
import itk.student.task.manager.dto.response.TaskResponse;
import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;
import itk.student.task.manager.service.AttachmentService;
import itk.student.task.manager.service.CommentService;
import itk.student.task.manager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Задачи — центральная сущность sandbox.
 * CRUD, статусы, assignee, bulk update, комментарии, вложения, history.
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Задачи, комментарии, вложения")
public class TaskController {

    private final TaskService taskService;
    private final CommentService commentService;
    private final AttachmentService attachmentService;

    @GetMapping
    @Operation(summary = "Список задач", description = "Фильтры: projectId, status, priority, assigneeId, tag, dueBefore, dueAfter")
    public PagedResponse<TaskResponse> listTasks(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) UUID tag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dueBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dueAfter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return taskService.listTasks(projectId, status, priority, assigneeId, tag, dueBefore, dueAfter, pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск задач", description = "Query min 2 chars, optional projectId")
    public PagedResponse<TaskResponse> searchTasks(
            @RequestParam String q,
            @RequestParam(required = false) UUID projectId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return taskService.searchTasks(q, projectId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создание задачи")
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.createTask(request);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Детали задачи", description = "Task + tags + counters")
    public TaskDetailResponse getTask(@PathVariable UUID taskId) {
        return taskService.getTask(taskId);
    }

    @PatchMapping("/{taskId}")
    @Operation(summary = "Обновление задачи", description = "Optimistic lock через version → 409 Conflict")
    public TaskResponse updateTask(@PathVariable UUID taskId, @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удаление задачи", description = "Soft delete по умолчанию, hard=true для физического удаления")
    public void deleteTask(@PathVariable UUID taskId,
                           @RequestParam(defaultValue = "false") boolean hard) {
        taskService.deleteTask(taskId, hard);
    }

    @PatchMapping("/{taskId}/status")
    @Operation(summary = "Смена статуса", description = "422 при недопустимом переходе статуса")
    public TaskResponse updateStatus(@PathVariable UUID taskId,
                                     @Valid @RequestBody UpdateTaskStatusRequest request) {
        return taskService.updateStatus(taskId, request);
    }

    @PatchMapping("/{taskId}/assignee")
    @Operation(summary = "Назначение исполнителя", description = "assigneeId=null снимает назначение")
    public TaskResponse updateAssignee(@PathVariable UUID taskId,
                                       @Valid @RequestBody UpdateTaskAssigneeRequest request) {
        return taskService.updateAssignee(taskId, request);
    }

    @PostMapping("/bulk-update")
    @Operation(summary = "Массовое обновление", description = "До 50 taskIds за запрос")
    public BulkUpdateTasksResponse bulkUpdate(@Valid @RequestBody BulkUpdateTasksRequest request) {
        return taskService.bulkUpdate(request);
    }

    @PostMapping("/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить комментарий к задаче")
    public CommentResponse createComment(@PathVariable UUID taskId,
                                         @Valid @RequestBody CreateCommentRequest request) {
        return commentService.createComment(taskId, request);
    }

    @GetMapping("/{taskId}/comments")
    @Operation(summary = "Комментарии задачи")
    public PagedResponse<CommentResponse> listComments(
            @PathVariable UUID taskId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return commentService.listComments(taskId, pageable);
    }

    @GetMapping("/{taskId}/history")
    @Operation(summary = "История изменений задачи")
    public List<ActivityLogResponse> history(@PathVariable UUID taskId) {
        return taskService.getHistory(taskId);
    }

    @PostMapping(value = "/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Загрузка вложения", description = "multipart/form-data, поле file")
    public AttachmentResponse uploadAttachment(@PathVariable UUID taskId,
                                               @RequestParam("file") MultipartFile file) {
        return attachmentService.upload(taskId, file);
    }
}
