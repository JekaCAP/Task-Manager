package itk.student.task.manager.dto.request;

import itk.student.task.manager.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddProjectMemberRequest(
        @NotNull UUID userId,
        @NotNull ProjectRole role
) {
}
