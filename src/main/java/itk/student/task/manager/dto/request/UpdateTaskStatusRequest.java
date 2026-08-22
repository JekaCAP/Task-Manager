package itk.student.task.manager.dto.request;

import itk.student.task.manager.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull TaskStatus status,
        @NotNull Long version
) {
}
