package itk.student.task.manager.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        String key,
        boolean archived,
        UUID ownerId,
        Instant createdAt,
        Instant updatedAt
) {
}
