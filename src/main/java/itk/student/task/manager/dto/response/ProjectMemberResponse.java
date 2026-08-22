package itk.student.task.manager.dto.response;

import itk.student.task.manager.enums.ProjectRole;

import java.time.Instant;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID id,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        ProjectRole role,
        Instant joinedAt
) {
}
