package itk.student.task.manager.dto.request;

import itk.student.task.manager.enums.TaskPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateTaskRequest(
        @Size(max = 255) String title,
        @Size(max = 4000) String description,
        TaskPriority priority,
        Instant dueDate,
        @NotNull Long version
) {
}
