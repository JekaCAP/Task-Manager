package itk.student.task.manager.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        UUID taskId,
        String originalName,
        String contentType,
        long sizeBytes,
        UUID uploadedById,
        Instant createdAt
) {
}
