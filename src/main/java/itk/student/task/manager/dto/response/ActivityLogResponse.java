package itk.student.task.manager.dto.response;

import itk.student.task.manager.enums.ActivityAction;

import java.time.Instant;
import java.util.UUID;

public record ActivityLogResponse(
        UUID id,
        UUID taskId,
        UUID actorId,
        String actorName,
        ActivityAction action,
        String details,
        Instant createdAt
) {
}
