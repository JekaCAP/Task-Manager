package itk.student.task.manager.dto.request;

import itk.student.task.manager.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateTaskRequest(
        @NotNull UUID projectId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 4000) String description,
        @NotNull TaskPriority priority,
        Instant dueDate,
        UUID assigneeId
) {
}
