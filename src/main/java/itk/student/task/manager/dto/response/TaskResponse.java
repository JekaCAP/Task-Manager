package itk.student.task.manager.dto.response;

import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UUID assigneeId,
        Instant dueDate,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
