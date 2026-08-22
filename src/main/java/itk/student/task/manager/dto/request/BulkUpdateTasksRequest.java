package itk.student.task.manager.dto.request;

import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record BulkUpdateTasksRequest(
        @NotEmpty @Size(max = 50) List<UUID> taskIds,
        TaskStatus status,
        TaskPriority priority,
        UUID assigneeId
) {
}
