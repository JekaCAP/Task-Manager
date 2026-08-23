package itk.student.task.manager.aqa.reference.dto;

import java.util.UUID;

public record CreateTaskRequest(
        UUID projectId,
        String title,
        String description,
        String priority
) {
}
