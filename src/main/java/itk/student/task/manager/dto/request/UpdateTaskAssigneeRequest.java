package itk.student.task.manager.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateTaskAssigneeRequest(
        UUID assigneeId,
        @NotNull Long version
) {
}
