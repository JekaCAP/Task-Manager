package itk.student.task.manager.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID taskId,
        UUID authorId,
        String authorName,
        String body,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
