package itk.student.task.manager.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TagResponse(
        UUID id,
        UUID projectId,
        String name,
        String color,
        Instant createdAt
) {
}
